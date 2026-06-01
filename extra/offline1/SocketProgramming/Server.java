package SocketProgramming;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Server {
    // Configuration parameters
    public static final int MAX_BUFFER_SIZE = 10 * 1024 * 1024; // 10 MB
    public static final int MIN_CHUNK_SIZE = 1024; // 1 KB
    public static final int MAX_CHUNK_SIZE = 1024 * 100; // 100 KB
    public static final int PORT = 6666;
    
    // Server state management
    private static Map<String, ClientHandler> connectedClients = new HashMap<>();
    private static Set<String> registeredUsers = new HashSet<>();
    private static Map<String, Long> currentBufferSize = new HashMap<>();
    private static int fileIDCounter = 0;
    
    public static void main(String[] args) throws IOException {
        ServerSocket welcomeSocket = new ServerSocket(PORT);
        System.out.println("Server started on port " + PORT);
        System.out.println("MAX_BUFFER_SIZE: " + MAX_BUFFER_SIZE);
        System.out.println("MIN_CHUNK_SIZE: " + MIN_CHUNK_SIZE);
        System.out.println("MAX_CHUNK_SIZE: " + MAX_CHUNK_SIZE);
        
        while (true) {
            System.out.println("Waiting for connection...");
            Socket socket = welcomeSocket.accept();
            System.out.println("Connection established from: " + socket.getInetAddress());
            
            // Create a new thread to handle the client
            Thread clientHandler = new ClientHandler(socket);
            clientHandler.start();
        }
    }
    
    // Synchronized method to register a new client
    public static synchronized boolean registerClient(String username, ClientHandler handler) {
        if (connectedClients.containsKey(username)) {
            return false; // Username already connected
        }
        connectedClients.put(username, handler);
        registeredUsers.add(username);
        currentBufferSize.put(username, 0L);
        return true;
    }
    
    // Synchronized method to unregister a client
    public static synchronized void unregisterClient(String username) {
        connectedClients.remove(username);
    }
    
    // Get list of all registered users
    public static synchronized Set<String> getRegisteredUsers() {
        return new HashSet<>(registeredUsers);
    }
    
    // Get list of connected clients
    public static synchronized Set<String> getConnectedClients() {
        return new HashSet<>(connectedClients.keySet());
    }
    
    // Check if buffer can accommodate new file
    public static synchronized boolean canAccommodateFile(String username, long fileSize) {
        long currentSize = currentBufferSize.getOrDefault(username, 0L);
        return (currentSize + fileSize) <= MAX_BUFFER_SIZE;
    }
    
    // Update buffer size
    public static synchronized void updateBufferSize(String username, long fileSize) {
        currentBufferSize.put(username, currentBufferSize.getOrDefault(username, 0L) + fileSize);
    }
    
    // Generate unique file ID
    public static synchronized int generateFileID() {
        return ++fileIDCounter;
    }
    
    // Send message to specific client
    public static void sendMessageToClient(String username, String message) {
        ClientHandler handler = connectedClients.get(username);
        if (handler != null) {
            handler.sendMessage(message);
        }
    }
    
    // Broadcast message to all connected clients
    public static void broadcastMessage(String message) {
        for (ClientHandler handler : connectedClients.values()) {
            handler.sendMessage(message);
        }
    }
}
