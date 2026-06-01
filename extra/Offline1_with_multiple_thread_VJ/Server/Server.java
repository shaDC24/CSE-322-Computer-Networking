package Server;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.io.File;
import java.io.FileInputStream;


public class Server 
{
    public static final int SOCKET_PORT = 6666;

    public static final long MAX_BUFFER_SIZE = 100000*1024;
    public static final int MAX_CHUNK_SIZE = 100*1024;
    public static final int MIN_CHUNK_SIZE = 100;



    public static long usedBufferSize = 0;



    //private  static HashSet<String> allClients = new HashSet<>();
    private  static HashMap<String,Worker> currentClients = new HashMap<>();//online clients
    private  static HashSet<String> offlineClients = new HashSet<>();//offline clients

    public static HashMap<String, FileUploadInfo> fileMap = new HashMap<>();//client-name,file-uploaded-object
    private static HashMap<String, FileRequest> fileRequestMap = new HashMap<>();//sender,request-object
    private static HashMap<String, ArrayList<FileRequest>> fileRequestHashMap = new HashMap<>();
    private static HashMap<String, Long> currentBufferSize = new HashMap<>();
    public static String fileMapStorage = "Server/FileMapStorage.txt";
    private static String fileRequestStorage = "Server/FileRequestStorage.txt";

    

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        ServerSocket welcomeSocket = new ServerSocket(SOCKET_PORT);
        initiationOfflineClients();
        initiationFileMapStorage();


        System.out.println("DEBUG :: Server started on port " + SOCKET_PORT);
        System.out.println("DEBUG :: MAX_BUFFER_SIZE: " + MAX_BUFFER_SIZE);
        System.out.println("DEBUG :: MIN_CHUNK_SIZE: " + MIN_CHUNK_SIZE);
        System.out.println("DEBUG :: MAX_CHUNK_SIZE: " + MAX_CHUNK_SIZE);        


        while(true) {
            System.out.println("Waiting for connection...");
            Socket socket = welcomeSocket.accept();
            System.out.println("A new client is wanted to connect.");

            Thread worker = new Worker(socket);
            worker.start();
        }

    }

    public static boolean loggedInClient(String clientName, Worker worker)
    {
        if(currentClients.containsKey(clientName))
        {
            return false;
        }
        if(offlineClients.contains(clientName))
        {
            offlineClients.remove(clientName);
        }
        currentClients.put(clientName,worker);

        long clientBufferSize =0;
        for(FileUploadInfo fui : fileMap.values())
        {
            if(fui.clientName.equals(clientName))
            {
                clientBufferSize += fui.fileSize;
            }
        }
        currentBufferSize.put(clientName,clientBufferSize);
        System.out.println("DEBUG :: Client " + clientName + " logged in. Buffer size allocated: " + clientBufferSize + " bytes");
        return true;

    }
    public static void loggedOutClient(String clientName)
    {
        currentClients.remove(clientName);
        offlineClients.add(clientName);
    }
    public static ArrayList<String> getOfflineClients()
    {
        return new ArrayList<>(offlineClients);
    }

    public static ArrayList<String> getOnlineClients()
    {
        return new ArrayList<>(currentClients.keySet());
    }

    public static boolean verifyBufferSize(String clientName, long fileSize)
    {
        long curSize=currentBufferSize.get(clientName);
        if((curSize + fileSize) <= MAX_BUFFER_SIZE)
        {
            return true;
        }
        return false;

    }
    public static void setBufferSize(String clientName, long fileSize)
    {
        long curSize=currentBufferSize.get(clientName);
        currentBufferSize.put(clientName,curSize+fileSize);
 
    }
    public static String getFileId()
    {
        return "FILE_"+System.currentTimeMillis();
    }
    public static String getRequestedId()
    {
        return "REQ_FILE_"+System.currentTimeMillis();
    }    
 
    public static void sendMessageToClient(String clientName, String message) {
        Worker worker = currentClients.get(clientName);
        if (worker != null) 
        {
            worker.sendMessage(message);
        }
        else
        {
            System.out.println("Offline client will get message");

        }
    }
    
    public static void broadcastMessage(String message) 
    {
        for (Worker worker : currentClients.values()) 
        {
            worker.sendMessage(message);
        }

        
    }    
    private static void initiationOfflineClients() {

        String baseDirPathForServer = "Server/ServerFiles";
        File baseDirServer = new File(baseDirPathForServer);

        if (!baseDirServer.exists() || !baseDirServer.isDirectory()) {
            System.out.println("No server files storage directory found. Creating a new path . ");
            baseDirServer.mkdirs();
            return;
        }

        File[] clientNameDirs = baseDirServer.listFiles();
        if (clientNameDirs != null) {
            for (File clientDir : clientNameDirs) {
                if (clientDir.isDirectory()) {
                    String clientName = clientDir.getName();
                    offlineClients.add(clientName);
                }
            }
        }
        System.out.println("DEBUG :: Offline clients loaded : " + offlineClients);
    }


    private static void initiationFileMapStorage() 
    {
        File file = new File(fileMapStorage);
        ArrayList<ArrayList<String>> fileDate = CommonFunctionUtility.fileToHashMap(file);
        
        for (ArrayList<String> oneData: fileDate) 
        {
            if (oneData.size() < 7) 
            {
                continue;
            }
            String fileId = oneData.get(0);
            String fileName = oneData.get(1);
            long fileSize = Long.parseLong(oneData.get(2));
            String accessType = oneData.get(3);
            String requestId = oneData.get(4);
            String clientName = oneData.get(5);
            long totalBytesReceived = Long.parseLong(oneData.get(6));
            
            FileUploadInfo fui = new FileUploadInfo(fileId, fileName, fileSize, accessType, requestId, clientName);
            fui.totalBytesReceived = totalBytesReceived;
            fileMap.put(fileId, fui);
        }
        
        System.out.println("DEBUG :: fileMap loaded. Total files: " + fileMap.size());
    }
}
