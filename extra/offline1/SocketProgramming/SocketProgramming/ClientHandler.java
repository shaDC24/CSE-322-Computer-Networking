package SocketProgramming;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class ClientHandler extends Thread {
    private Socket socket;
    private String username;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String clientDirectory;
    private Map<Integer, UploadSession> uploadSessions; // fileID -> UploadSession
    private List<FileRequest> incomingRequests; // Incoming file requests
    
    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.uploadSessions = new HashMap<>();
        this.incomingRequests = new ArrayList<>();
    }
    
    @Override
    public void run() {
        try {
            // Initialize streams
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            
            // Step 1: Authentication - receive username
            String authMessage = (String) in.readObject();
            if (!authenticate(authMessage)) {
                sendMessage("ERROR: Username already connected or invalid format");
                socket.close();
                return;
            }
            
            System.out.println("Client authenticated: " + username);
            sendMessage("SUCCESS: Authenticated as " + username);
            
            // Step 2: Create client directory if it doesn't exist
            createClientDirectory();
            
            // Step 3: Handle client requests
            while (true) {
                String request = (String) in.readObject();
                if (request == null) break;
                
                handleClientRequest(request);
            }
            
        } catch (EOFException e) {
            System.out.println("Client " + username + " disconnected");
            handleClientDisconnection();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error handling client: " + e.getMessage());
            handleClientDisconnection();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    // Authenticate client with username
    private boolean authenticate(String usernameInput) {
        if (usernameInput == null || usernameInput.trim().isEmpty()) {
            return false;
        }
        
        this.username = usernameInput.trim();
        return Server.registerClient(username, this);
    }
    
    // Create client directory
    private void createClientDirectory() {
        this.clientDirectory = "ClientFiles/" + username;
        try {
            Files.createDirectories(Paths.get(clientDirectory));
            System.out.println("Directory created for: " + username);
        } catch (IOException e) {
            System.out.println("Error creating directory: " + e.getMessage());
        }
    }
    
    // Handle different client requests
    private void handleClientRequest(String request) {
        String[] parts = request.split(":", 2);
        String command = parts[0];
        String payload = parts.length > 1 ? parts[1] : "";
        
        switch (command) {
            case "LIST_USERS":
                handleListUsers();
                break;
            case "LIST_FILES":
                handleListFiles();
                break;
            case "LIST_PUBLIC_FILES":
                handleListPublicFiles(payload);
                break;
            case "UPLOAD":
                handleUploadRequest(payload);
                break;
            case "DOWNLOAD":
                handleDownloadRequest(payload);
                break;
            case "FILE_REQUEST":
                handleFileRequest(payload);
                break;
            case "VIEW_MESSAGES":
                handleViewMessages();
                break;
            case "VIEW_HISTORY":
                handleViewHistory();
                break;
            case "LOGOUT":
                handleLogout();
                break;
            default:
                sendMessage("ERROR: Unknown command: " + command);
        }
    }
    
    // List all registered users
    private void handleListUsers() {
        Set<String> registered = Server.getRegisteredUsers();
        Set<String> connected = Server.getConnectedClients();
        
        StringBuilder response = new StringBuilder("USERS_LIST:\n");
        for (String user : registered) {
            String status = connected.contains(user) ? "[ONLINE]" : "[OFFLINE]";
            response.append(user).append(" ").append(status).append("\n");
        }
        sendMessage(response.toString());
    }
    
    // List files of current user
    private void handleListFiles() {
        sendMessage("IMPLEMENT: List user's files (private and public)");
    }
    
    // List public files of another user
    private void handleListPublicFiles(String targetUser) {
        sendMessage("IMPLEMENT: List public files of " + targetUser);
    }
    
    // Handle upload request
    private void handleUploadRequest(String metadata) {
        // Metadata format: filename:filesize:isPublic[:requestID]
        String[] parts = metadata.split(":");
        if (parts.length < 3) {
            sendMessage("ERROR: Invalid upload metadata");
            return;
        }
        
        String filename = parts[0];
        long filesize = Long.parseLong(parts[1]);
        boolean isPublic = Boolean.parseBoolean(parts[2]);
        Integer requestID = parts.length > 3 ? Integer.parseInt(parts[3]) : null;
        
        // Check buffer size
        if (!Server.canAccommodateFile(username, filesize)) {
            sendMessage("ERROR: Buffer size exceeded");
            return;
        }
        
        // Generate fileID
        int fileID = Server.generateFileID();
        int chunkSize = generateRandomChunkSize();
        
        // Create upload session
        UploadSession session = new UploadSession(fileID, filename, filesize, chunkSize, isPublic, requestID);
        uploadSessions.put(fileID, session);
        
        // Send confirmation
        sendMessage("UPLOAD_CONFIRM:" + fileID + ":" + chunkSize);
    }
    
    // Handle download request
    private void handleDownloadRequest(String metadata) {
        // Metadata format: sourceUser:filename
        String[] parts = metadata.split(":", 2);
        if (parts.length < 2) {
            sendMessage("ERROR: Invalid download metadata");
            return;
        }
        
        String sourceUser = parts[0];
        String filename = parts[1];
        
        sendMessage("IMPLEMENT: Download " + filename + " from " + sourceUser);
    }
    
    // Handle file request
    private void handleFileRequest(String metadata) {
        // Metadata format: description:recipient
        String[] parts = metadata.split(":", 2);
        if (parts.length < 2) {
            sendMessage("ERROR: Invalid file request metadata");
            return;
        }
        
        String description = parts[0];
        String recipient = parts[1];
        
        sendMessage("IMPLEMENT: File request for " + description + " to " + recipient);
    }
    
    // View unread messages
    private void handleViewMessages() {
        StringBuilder response = new StringBuilder("MESSAGES:\n");
        if (incomingRequests.isEmpty()) {
            response.append("No unread messages\n");
        } else {
            for (FileRequest req : incomingRequests) {
                response.append(req.toString()).append("\n");
            }
        }
        sendMessage(response.toString());
    }
    
    // View upload/download history
    private void handleViewHistory() {
        sendMessage("IMPLEMENT: View upload/download history");
    }
    
    // Handle client logout
    private void handleLogout() {
        sendMessage("GOODBYE");
        handleClientDisconnection();
    }
    
    // Handle client disconnection
    private void handleClientDisconnection() {
        if (username != null) {
            Server.unregisterClient(username);
            // Clean up incomplete uploads
            cleanupIncompleteUploads();
            System.out.println("Client unregistered: " + username);
        }
    }
    
    // Clean up incomplete uploads when client disconnects
    private void cleanupIncompleteUploads() {
        for (UploadSession session : uploadSessions.values()) {
            // Delete incomplete chunks
            String sessionDir = clientDirectory + "/temp_" + session.fileID;
            // TODO: Implement cleanup logic
        }
        uploadSessions.clear();
    }
    
    // Generate random chunk size between MIN and MAX
    private int generateRandomChunkSize() {
        Random rand = new Random();
        return Server.MIN_CHUNK_SIZE + rand.nextInt(Server.MAX_CHUNK_SIZE - Server.MIN_CHUNK_SIZE + 1);
    }
    
    // Send message to client
    public void sendMessage(String message) {
        try {
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            System.out.println("Error sending message to " + username + ": " + e.getMessage());
        }
    }
    
    // Inner class to represent an upload session
    private static class UploadSession {
        int fileID;
        String filename;
        long totalFileSize;
        int chunkSize;
        boolean isPublic;
        Integer requestID;
        long receivedSize = 0;
        List<byte[]> chunks = new ArrayList<>();
        
        UploadSession(int fileID, String filename, long totalFileSize, int chunkSize, boolean isPublic, Integer requestID) {
            this.fileID = fileID;
            this.filename = filename;
            this.totalFileSize = totalFileSize;
            this.chunkSize = chunkSize;
            this.isPublic = isPublic;
            this.requestID = requestID;
        }
    }
    
    // Inner class to represent a file request
    private static class FileRequest {
        int requestID;
        String requesterUsername;
        String description;
        String recipient;
        long timestamp;
        
        FileRequest(int requestID, String requesterUsername, String description, String recipient) {
            this.requestID = requestID;
            this.requesterUsername = requesterUsername;
            this.description = description;
            this.recipient = recipient;
            this.timestamp = System.currentTimeMillis();
        }
        
        @Override
        public String toString() {
            return "RequestID: " + requestID + " | From: " + requesterUsername + " | Description: " + description;
        }
    }
}
