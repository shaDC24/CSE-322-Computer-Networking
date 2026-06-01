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

    public static final long MAX_BUFFER_SIZE = 100000*1024;
    public static final int MAX_CHUNK_SIZE = 100*1024;
    public static final int MIN_CHUNK_SIZE = 100;

    public static long usedBufferSize = 0;



    public static HashMap<String,Worker> currentClients = new HashMap<>();//online clients
    public static HashSet<String> offlineClients = new HashSet<>();//offline clients
    public static HashMap<String, FileUploadInfo> fileMap = new HashMap<>();
    public static HashMap<String, FileRequest> fileRequestMap = new HashMap<>();
    public static HashMap<String, FileRequest> pendingMap =new HashMap<>();
    public static String fileMapStorage = "Server/FileMapStorage.txt";
    //public static File messageFile = new File("Server/ServerFiles/"+clientName+"/unreadMessageFile.txt");

    


    
    

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        ServerSocket welcomeSocket = new ServerSocket(6666);
        initiationOfflineClients();
        initiationFileMapStorage();


        while(true) {
            System.out.println("Waiting for connection...");
            Socket socket = welcomeSocket.accept();
            System.out.println("A new client is wanted to connect.");

            Thread worker = new Worker(socket);
            worker.start();


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

    if (!file.exists()) {
        System.out.println("No FileMapStorage found. Creating new file.");
        return;
    }

    try (FileInputStream fis = new FileInputStream(file)) 
    {
        ArrayList<Byte> buffer = new ArrayList<>();
        int b;
        while ((b = fis.read()) != -1) 
        {
            if (b == '\n') {
                
                byte[] lineBytes = new byte[buffer.size()];
                for (int i = 0; i < buffer.size(); i++) 
                {
                    lineBytes[i] = buffer.get(i);
                }
                buffer.clear(); 

                String line = new String(lineBytes).trim();
                if (line.isEmpty()) 
                continue;

                String[] parts = line.split("\\|");
                if (parts.length < 7) 
                continue;

                String fileId = parts[0];
                String fileName = parts[1];
                long fileSize = Long.parseLong(parts[2]);
                String accessType = parts[3];
                String requestId = parts[4];
                String clientName = parts[5];
                long totalBytesReceived = Long.parseLong(parts[6]);

                FileUploadInfo fui = new FileUploadInfo(fileId, fileName, fileSize, accessType, requestId, clientName);
                fui.totalBytesReceived = totalBytesReceived;

                fileMap.put(fileId, fui);
            } 
            else 
            {
                buffer.add((byte) b);
            }
        }


        System.out.println("DEBUG :: fileMap loaded. Total files: " + fileMap.size());

    } catch (IOException e) 
    {
        e.printStackTrace();
    }
}



}
