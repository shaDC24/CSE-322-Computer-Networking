package Client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;
import java.util.ArrayList;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;


public class ClientUtil {
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Scanner scanner;
    private String clientName;

    public ClientUtil(ObjectOutputStream out, ObjectInputStream in, Scanner scanner, String clientName) {
        this.out = out;
        this.in = in;
        this.scanner = scanner;
        this.clientName = clientName;

    }

    public void showMenu() throws IOException , ClassNotFoundException
    {
        System.out.println("Menu:");
        System.out.println("1. Look-up Client Status (online/offline)");
        System.out.println("2. Look-up uploaded files by Client (public/private)");
        System.out.println("3. Download file");
        System.out.println("4. Look-up uploaded files by other Client (public)");
        System.out.println("5. Request a file");
        System.out.println("6. View unread messages");
        System.out.println("7. Upload a file");
        System.out.println("8. View history");
        System.out.println("0. Log-out");


        System.out.print("Enter your choice: ");
        int choice = Integer.parseInt(scanner.nextLine());

        out.writeObject(choice);

        if(choice==1)
        {
            ArrayList<String> clientsList = (ArrayList<String>)in.readObject();  

            System.out.println("\n\nClients who have connected to the server at least once : \n");

            for (String client : clientsList) 
            {
                 System.out.println(client);
            } 
        }
        else if(choice==2)
        {
            showSelfAllFiles(in);

        }
        else if(choice==3)
        {


            System.out.print("Enter the file Id you want to download : ");
            String fileId = scanner.nextLine();
            out.writeObject(fileId);
            String serverResponse = (String) in.readObject();
            System.out.println("Debug :: From Server # " + serverResponse);  
            if(serverResponse.contains("This is valid file ."))
            {
                System.out.println("Downloading file...");
                String fileName = (String) in.readObject();
                String downloadPath = "Client/DownLoadedFiles/" + clientName + "/";
                File downloadDirectory = new File(downloadPath);
                if (!downloadDirectory.exists()) 
                    downloadDirectory.mkdirs();
                FileOutputStream fileOutputStream = new FileOutputStream(downloadPath+fileName);
                while(true)
                {
                    Object response = in.readObject();
                    if (response instanceof String && ((String) response).contains("download completed")) 
                    {
                        System.out.println("Download complete!");
                        break;
                    }

                    byte[] chunkBytes = (byte[]) response;
                    fileOutputStream.write(chunkBytes);
                    
                }
                fileOutputStream.close();


            }
            else
            {
                System.out.println("From Server # " + serverResponse);  
            }


        }
        else if(choice==4)
        {  
            showOthersPublicFiles(in);
            
        }
        else if(choice==5)
        {
            String responseFromServer=(String) in.readObject();
            System.out.println("From server # "+responseFromServer);
            String description = scanner.nextLine();
            out.writeObject(description);
            responseFromServer=(String) in.readObject();
            System.out.println("From server # "+responseFromServer);
            String receiver = scanner.nextLine();
            out.writeObject(receiver);
            responseFromServer = (String) in.readObject();
            System.out.println("From Server # "+responseFromServer);
        }
        else if(choice==6)
        {
            Object responseFromServer = in.readObject();
            if(responseFromServer instanceof String)
            {
                System.out.println((String) responseFromServer);
            }
            else if(responseFromServer instanceof ArrayList)
            {
                ArrayList<String> messages = (ArrayList<String>)responseFromServer;
                System.out.println("\nUnread Messages :");
                for(String msg : messages)
                {
                    System.out.println(msg);
                }
            }            
        }
        else if(choice==7)
        {
            uploadFile();
        }
        else if(choice==8)
        {
            Object responseFromServer = in.readObject();
            if(responseFromServer instanceof String)
            {
                System.out.println((String) responseFromServer);
            }
            else if(responseFromServer instanceof ArrayList)
            {
                ArrayList<String> history = (ArrayList<String>)responseFromServer;
                System.out.println("\nYour History:");
                for(String h : history)
                {
                    System.out.println(h);
                }
               
            }            
        }
        else if(choice==0)
        {
            System.out.println("Logging out...");
            System.exit(0);
        }
     


    }
    public void uploadFile() throws IOException , ClassNotFoundException
     {

        SampleFileUtil.createSampleFiles();

        String fileAccessType=null;

        System.out.println("\nIs this file to be uploaded as a request file?");
        System.out.println("1. Yes");
        System.out.println("0. No");
        System.out.print("Enter your choice (1/0): ");
        int responseChoice = Integer.parseInt(scanner.nextLine());  
        if (responseChoice != 0 && responseChoice != 1) {
            System.out.println("Invalid request choice!");
            out.writeObject("Upload cancel .");
            return;
        }    

        String requestId = null;
        if (responseChoice == 1) 
        {
            fileAccessType = "Public";
            System.out.print("Enter the Request ID: ");
            requestId = scanner.nextLine().trim();
            System.out.println("DEBUG :: "+requestId);

        }

        System.out.println("Upload File Options:");
        System.out.println("1. Enter file path manually");
        System.out.println("0. Use sample file Gui");

        System.out.print("Enter choice (1/0): ");
        int choiceForFile = Integer.parseInt(scanner.nextLine());
        File file = null;
        if (choiceForFile == 1) 
        {
            System.out.print("Enter the file path: ");
            String filePathName = scanner.nextLine();
            file = new File(filePathName);
            if (!file.exists()) 
            {
                System.out.println("File not found!");
                out.writeObject("Upload cancel .");
                return;
            }
        }
        else if (choiceForFile == 0) 
        {
            file = SampleFileUtil.chooseSampleFile(scanner);
            if (!file.exists()) 
            {
                System.out.println("Sample file not found!");
                out.writeObject("Upload cancel .");
                return;
            }
        } 
        else 
        {
            System.out.println("Invalid choice!");
            out.writeObject("Upload cancel .");
            return;
        }
        if(responseChoice == 0)
        {
            System.out.println("\nChoose access type:");
            System.out.println("1. Public");
            System.out.println("0. Private");
            System.out.print("Enter your choice (1/0): ");
            int fileAccessChoice = Integer.parseInt(scanner.nextLine());
            if (fileAccessChoice != 0 && fileAccessChoice != 1) 
            {
                System.out.println("Invalid access choice!");
                out.writeObject("Upload cancel .");
                return;
            }
            fileAccessType = (fileAccessChoice == 1) ? "Public" : "Private";
        }
        long fileSize = file.length();
        out.writeObject("Client "+clientName+" wants to upload a file.");
        out.writeObject(file.getName());
        out.writeObject(fileSize);
        out.writeObject(fileAccessType);
        out.writeObject(requestId); 
        String msgFromServer = (String) in.readObject();
        System.out.println("From Server # " + msgFromServer);
        
        if (!msgFromServer.contains("accepted")) 
        {
            System.out.println("Upload request rejected by the server.");
            return;
        }
        
        int fileChunkSize = (int) in.readObject();
        String fileId = (String) in.readObject();

        FileInputStream fileInputStream = new FileInputStream(file);
        byte[] buffer = new byte[fileChunkSize];
        int bytesRead = fileInputStream.read(buffer);
        long totalBytesSent = 0;

        while(bytesRead != -1)
        {
            byte[] chunk = new byte[bytesRead];  
            System.arraycopy(buffer, 0, chunk, 0, bytesRead);  
            out.writeObject(chunk);
            totalBytesSent += bytesRead;
            System.out.println("Sending to Server " + totalBytesSent + " out of " + fileSize + " bytes.");
            String acknowledgmentFromServer = (String) in.readObject();
            System.out.println("From Server # " + acknowledgmentFromServer);
            if(!acknowledgmentFromServer.contains("received"))
            {
                System.out.println("Error in file upload and so Terminating upload.");
                fileInputStream.close();
                return;
            }
            bytesRead = fileInputStream.read(buffer);
        }
        fileInputStream.close();
        out.writeObject("File upload completed for file ID: " + fileId);

        String fileUploadResponseFromServer = (String) in.readObject();
        if(fileUploadResponseFromServer.contains("successfully"))
        {
            System.out.println("Server response: " + fileUploadResponseFromServer);
            if(requestId!=null)
            {
                out.writeObject("notify request sender.");

            }
        }
        else
        {
            System.out.println("File upload failed at server side.");
        }    
  

    }

    // public void uploadFile() throws IOException , ClassNotFoundException
    //  {

    //     SampleFileUtil.createSampleFiles();

    //     System.out.println("Upload File Options:");
    //     System.out.println("1. Enter file path manually");
    //     System.out.println("0. Use sample file Gui");

    //     System.out.print("Enter choice (1/0): ");
    //     int choiceForFile = Integer.parseInt(scanner.nextLine());

    //     File file = null;

    //     if (choiceForFile == 1) 
    //     {
    //         System.out.print("Enter the file path: ");
    //         String filePathName = scanner.nextLine();
    //         file = new File(filePathName);
    //         if (!file.exists()) 
    //         {
    //             System.out.println("File not found!");
    //             out.writeObject("Upload cancel .");
    //             return;
    //         }
    //     }
    //     else if (choiceForFile == 0) 
    //     {
    //         file = SampleFileUtil.chooseSampleFile(scanner);
    //         if (!file.exists()) 
    //         {
    //             System.out.println("Sample file not found!");
    //             out.writeObject("Upload cancel .");
    //             return;
    //         }
    //     } 
    //     else 
    //     {
    //         System.out.println("Invalid choice!");
    //         out.writeObject("Upload cancel .");
    //         return;
    //     }
    //     System.out.println("\nChoose access type:");
    //     System.out.println("1. Public");
    //     System.out.println("0. Private");
    //     System.out.print("Enter your choice (1/0): ");
    //     int fileAccessChoice = Integer.parseInt(scanner.nextLine());
    //     if (fileAccessChoice != 0 && fileAccessChoice != 1) 
    //     {
    //         System.out.println("Invalid access choice!");
    //         out.writeObject("Upload cancel .");
    //         return;
    //     }
    //     String fileAccessType = (fileAccessChoice == 1) ? "Public" : "Private";
    //     System.out.println("\nIs this file to be uploaded as a request file?");
    //     System.out.println("1. Yes");
    //     System.out.println("0. No");
    //     System.out.print("Enter your choice (1/0): ");
    //     int responseChoice = Integer.parseInt(scanner.nextLine());  
    //     if (responseChoice != 0 && responseChoice != 1) {
    //         System.out.println("Invalid request choice!");
    //         out.writeObject("Upload cancel .");
    //         return;
    //     }         
    //     String requestId = null;

    //     if (responseChoice == 1) 
    //     {
    //         fileAccessType = "Public";
    //         System.out.print("Enter the Request ID: ");
    //         requestId = scanner.nextLine().trim();
    //         System.out.println("DEBUG :: "+requestId);

    //     }

    //     long fileSize = file.length();
    //     out.writeObject("Client "+clientName+" wants to upload a file.");
    //     out.writeObject(file.getName());
    //     out.writeObject(fileSize);
    //     out.writeObject(fileAccessType);
    //     out.writeObject(requestId); 


    //     String msgFromServer = (String) in.readObject();
    //     System.out.println("From Server # " + msgFromServer);
    //     if (!msgFromServer.contains("accepted")) 
    //     {
    //         System.out.println("Upload request rejected by the server.");
    //         return;
    //     }
        
    //         int fileChunkSize = (int) in.readObject();
    //         String fileId = (String) in.readObject();

    //         FileInputStream fileInputStream = new FileInputStream(file);
    //         byte[] buffer = new byte[fileChunkSize];
    //         int bytesRead = fileInputStream.read(buffer);
    //         long totalBytesSent = 0;
    //         while(bytesRead != -1)
    //         {
    //             byte[] chunk = new byte[bytesRead];  
    //             System.arraycopy(buffer, 0, chunk, 0, bytesRead);  
    //             out.writeObject(chunk);
    //             totalBytesSent += bytesRead;
    //             System.out.println("Sending to Server " + totalBytesSent + " out of " + fileSize + " bytes.");
    //             String acknowledgmentFromServer = (String) in.readObject();
    //             System.out.println("From Server # " + acknowledgmentFromServer);
    //             if(!acknowledgmentFromServer.contains("received"))
    //             {
    //                 System.out.println("Error in file upload and so Terminating upload.");
    //                 fileInputStream.close();
    //                 return;
    //             }
    //             bytesRead = fileInputStream.read(buffer);
    //         }
    //         fileInputStream.close();
    //         out.writeObject("File upload completed for file ID: " + fileId);

    //         String fileUploadResponseFromServer = (String) in.readObject();
    //         if(fileUploadResponseFromServer.contains("successfully"))
    //         {
    //             System.out.println("Server response: " + fileUploadResponseFromServer);
    //             if(requestId!=null)
    //             {
    //                 out.writeObject("notify request sender.");

    //             }
    //         }
    //         else
    //         {
    //             System.out.println("File upload failed at server side.");
    //         }    
  

    // }

    private void showSelfAllFiles(ObjectInputStream in) throws IOException , ClassNotFoundException
    {
            Object responseFromServer = in.readObject();
            if(responseFromServer instanceof String)
            {
                System.out.println((String) responseFromServer);

            }
            else if(responseFromServer instanceof ArrayList)
            {

                ArrayList<String> fileList = (ArrayList<String>)responseFromServer;
                if(fileList.isEmpty())
                {
                    System.out.println("No files found for you.");
                }
                else
                {
                    System.out.println("Uploaded Files By YOU : ");
                    for(String f : fileList)
                    {
                        System.out.println(f);
                    }
                }
                
            }
            else
            {
                System.out.println("Not expectedresult from server.");
            }

    }

    private void showOthersPublicFiles(ObjectInputStream in) throws IOException , ClassNotFoundException
    {
            Object responseFromServer = in.readObject();
            if(responseFromServer instanceof String)
            {
                System.out.println((String) responseFromServer);

            }
            else if(responseFromServer instanceof ArrayList)
            {

                ArrayList<String> fileList = (ArrayList<String>)responseFromServer;
                if(fileList.isEmpty())
                {
                    System.out.println("No files found from others public folder.");
                }
                else
                {
                    System.out.println("Uploaded Files By Others (Public) : ");
                    for(String f : fileList)
                    {
                        System.out.println(f);
                    }
                }
                
            }
            else
            {
                System.out.println("Not expected result from server.");
            }         
    }
/*public void startNotificationListener() {
    Thread listenerThread = new Thread(() -> {
        try {
            while (true) {
                // Non-blocking check for incoming messages
                if (in.available() > 0) {
                    Object notification = in.readObject();
                    if (notification instanceof String) {
                        String msg = (String) notification;
                        System.out.println("\n\n*** NOTIFICATION *** ");
                        System.out.println(msg);
                        System.out.println("*** END NOTIFICATION ***\n");
                    }
                }
                Thread.sleep(500); // Check every 500ms
            }
        } catch (Exception e) {
            System.out.println("Notification listener stopped.");
        }
    });
    listenerThread.setDaemon(true);
    listenerThread.start();
}*/
public void startNotificationListener() {
    Thread listenerThread = new Thread(() -> {
        try {
            while (true) {
                Object obj = in.readObject();
                
                if (obj instanceof String) {
                    String msg = (String) obj;
                    
                    // Check if it's a notification
                    if (msg.startsWith("NOTIFICATION:")) {
                        // Remove the prefix and display
                        String actualMessage = msg.substring("NOTIFICATION:".length());
                        System.out.println("\n\n╔════════════════════════════════════╗");
                        System.out.println("║      *** NOTIFICATION ***          ║");
                        System.out.println("╚════════════════════════════════════╝");
                        System.out.println(actualMessage);
                        System.out.println("════════════════════════════════════\n");
                        System.out.print("Press Enter to continue...");
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Notification listener stopped.");
        }
    });
    listenerThread.setDaemon(true);
    listenerThread.start();
}
 

}