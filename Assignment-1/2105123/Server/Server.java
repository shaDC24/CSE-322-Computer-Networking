package Server;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.io.File;
import java.io.FileInputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.Map;
import java.util.Set;


public class Server 
{

    public static final long MAX_BUFFER_SIZE = 500*1024*1024;//100;
    public static final int MAX_CHUNK_SIZE = 100*1024;//50;
    public static final int MIN_CHUNK_SIZE = 10;

    public static long usedBufferSize = 0;



    /*public static HashMap<String,Worker> currentClients = new HashMap<>();//online clients
    public static HashSet<String> offlineClients = new HashSet<>();//offline clients
    public static HashMap<String, FileUploadInfo> fileMap = new HashMap<>();
    public static HashMap<String, FileRequest> fileRequestMap = new HashMap<>();*/

    public static Map<String, Worker> currentClients = new ConcurrentHashMap<>();
    public static Set<String> offlineClients = new CopyOnWriteArraySet<>();
    public static Map<String, FileUploadInfo> fileMap = new ConcurrentHashMap<>();
    public static Map<String, FileRequest> fileRequestMap = new ConcurrentHashMap<>();

    public static String fileMapStorage = "Server/FileMapStorage.txt";

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        ServerSocket welcomeSocket = new ServerSocket(6666);
        initiationOfflineClients();
        initiationFileMapStorage();

        while(true) 
        {
            System.out.println("Waiting for connection...");
            Socket socket = welcomeSocket.accept();
            System.out.println("A new client is wanted to connect.");

            Thread worker = new Worker(socket);
            worker.start();


        }

    }

    private static void initiationOfflineClients()
    {

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
    //  System.out.println("DEBUG :: Offline clients loaded : " + offlineClients);


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
        
      //  System.out.println("DEBUG :: fileMap loaded. Total files: " + fileMap.size());
    }


}




