package SocketProgramming;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String username;
    
    public Client(String host, int port) throws IOException {
        this.socket = new Socket(host, port);
        System.out.println("Connection established to server: " + host + ":" + port);
        System.out.println("Local port: " + socket.getLocalPort());
        
        // Initialize streams
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
    }
    
    public static void main(String[] args) throws IOException {
        Client client = new Client("localhost", 6666);
        client.run();
    }
    
    public void run() {
        Scanner scanner = new Scanner(System.in);
        
        try {
            // Step 1: Authentication
            if (!authenticate(scanner)) {
                System.out.println("Authentication failed. Exiting.");
                socket.close();
                return;
            }
            
            // Start thread to receive messages from server
            Thread receiverThread = new Thread(new MessageReceiver());
            receiverThread.setDaemon(true);
            receiverThread.start();
            
            // Step 2: Display menu and handle user commands
            displayMenu();
            
            while (true) {
                System.out.print("\nEnter command: ");
                String command = scanner.nextLine().trim();
                
                if (command.isEmpty()) continue;
                
                handleCommand(command, scanner);
            }
            
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error: " + e.getMessage());
        } finally {
            try {
                socket.close();
                scanner.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    // Authenticate with username
    private boolean authenticate(Scanner scanner) throws IOException, ClassNotFoundException {
        System.out.print("Enter username: ");
        this.username = scanner.nextLine().trim();
        
        if (username.isEmpty()) {
            System.out.println("Username cannot be empty");
            return false;
        }
        
        // Send username to server
        out.writeObject(username);
        out.flush();
        
        // Receive authentication response
        String response = (String) in.readObject();
        System.out.println(response);
        
        return response.contains("SUCCESS");
    }
    
    // Display command menu
    private void displayMenu() {
        System.out.println("\n========== FILE SERVER MENU ==========");
        System.out.println("1. LIST_USERS                         - List all users (online/offline)");
        System.out.println("2. LIST_FILES                         - List your files (private/public)");
        System.out.println("3. LIST_PUBLIC_FILES <username>       - List public files of a user");
        System.out.println("4. UPLOAD <filepath> <public|private> - Upload a file");
        System.out.println("5. UPLOAD_REQUEST <filepath> <requestID> <public|private> - Upload as response to request");
        System.out.println("6. DOWNLOAD <username> <filename>     - Download a file from a user");
        System.out.println("7. FILE_REQUEST <description> <username|ALL> - Request a file from user(s)");
        System.out.println("8. VIEW_MESSAGES                      - View unread file request messages");
        System.out.println("9. VIEW_HISTORY                       - View upload/download history");
        System.out.println("10. LOGOUT                            - Disconnect from server");
        System.out.println("=====================================\n");
    }
    
    // Handle user commands
    private void handleCommand(String command, Scanner scanner) throws IOException {
        String[] parts = command.split("\\s+", 2);
        String cmd = parts[0].toUpperCase();
        String payload = parts.length > 1 ? parts[1] : "";
        
        switch (cmd) {
            case "LIST_USERS":
            case "1":
                sendCommand("LIST_USERS");
                break;
                
            case "LIST_FILES":
            case "2":
                sendCommand("LIST_FILES");
                break;
                
            case "LIST_PUBLIC_FILES":
            case "3":
                if (payload.isEmpty()) {
                    System.out.println("Usage: LIST_PUBLIC_FILES <username>");
                } else {
                    sendCommand("LIST_PUBLIC_FILES:" + payload);
                }
                break;
                
            case "UPLOAD":
            case "4":
                handleUpload(payload, scanner);
                break;
                
            case "UPLOAD_REQUEST":
            case "5":
                handleUploadRequest(payload, scanner);
                break;
                
            case "DOWNLOAD":
            case "6":
                if (payload.isEmpty()) {
                    System.out.println("Usage: DOWNLOAD <username> <filename>");
                } else {
                    sendCommand("DOWNLOAD:" + payload);
                }
                break;
                
            case "FILE_REQUEST":
            case "7":
                handleFileRequest(payload, scanner);
                break;
                
            case "VIEW_MESSAGES":
            case "8":
                sendCommand("VIEW_MESSAGES");
                break;
                
            case "VIEW_HISTORY":
            case "9":
                sendCommand("VIEW_HISTORY");
                break;
                
            case "LOGOUT":
            case "10":
                sendCommand("LOGOUT");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.exit(0);
                break;
                
            default:
                System.out.println("Unknown command: " + cmd);
                displayMenu();
        }
    }
    
    // Handle file upload
    private void handleUpload(String payload, Scanner scanner) throws IOException {
        System.out.print("Enter file path: ");
        String filepath = scanner.nextLine().trim();
        System.out.print("Is file public? (yes/no): ");
        String isPublic = scanner.nextLine().trim().equalsIgnoreCase("yes") ? "true" : "false";
        
        File file = new File(filepath);
        if (!file.exists()) {
            System.out.println("File not found: " + filepath);
            return;
        }
        
        String filename = file.getName();
        long filesize = file.length();
        
        // Send upload request
        String uploadMetadata = filename + ":" + filesize + ":" + isPublic;
        sendCommand("UPLOAD:" + uploadMetadata);
    }
    
    // Handle upload in response to a request
    private void handleUploadRequest(String payload, Scanner scanner) throws IOException {
        System.out.print("Enter file path: ");
        String filepath = scanner.nextLine().trim();
        System.out.print("Enter request ID: ");
        String requestID = scanner.nextLine().trim();
        
        File file = new File(filepath);
        if (!file.exists()) {
            System.out.println("File not found: " + filepath);
            return;
        }
        
        String filename = file.getName();
        long filesize = file.length();
        
        // Upload as public file with request ID
        String uploadMetadata = filename + ":" + filesize + ":true:" + requestID;
        sendCommand("UPLOAD:" + uploadMetadata);
    }
    
    // Handle file request
    private void handleFileRequest(String payload, Scanner scanner) throws IOException {
        System.out.print("Enter file description: ");
        String description = scanner.nextLine().trim();
        System.out.print("Enter recipient username (or ALL for broadcast): ");
        String recipient = scanner.nextLine().trim();
        
        if (description.isEmpty() || recipient.isEmpty()) {
            System.out.println("Description and recipient cannot be empty");
            return;
        }
        
        String requestMetadata = description + ":" + recipient;
        sendCommand("FILE_REQUEST:" + requestMetadata);
    }
    
    // Send command to server
    private void sendCommand(String command) throws IOException {
        try {
            out.writeObject(command);
            out.flush();
            System.out.println("Command sent: " + command.split(":")[0]);
        } catch (IOException e) {
            System.out.println("Error sending command: " + e.getMessage());
        }
    }
    
    // Inner class to receive messages from server
    private class MessageReceiver implements Runnable {
        @Override
        public void run() {
            try {
                while (true) {
                    String message = (String) in.readObject();
                    if (message == null) break;
                    
                    System.out.println("\n[SERVER] " + message);
                    System.out.print("Enter command: ");
                }
            } catch (EOFException e) {
                System.out.println("\nConnection closed by server");
                System.exit(0);
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Error receiving message: " + e.getMessage());
            }
        }
    }
}
