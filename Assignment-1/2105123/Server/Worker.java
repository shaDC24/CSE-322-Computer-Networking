package Server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.io.File;
import java.util.ArrayList;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.time.LocalDateTime; 
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class Worker extends Thread 
{
    Socket socket;
    ObjectInputStream in;
    ObjectOutputStream out;
    String clientName;
 

    public Worker(Socket socket)
    {
        this.socket = socket;
        this.clientName=null;

    }

    public void run()
    {
        
        try {
            out = new ObjectOutputStream(this.socket.getOutputStream());
            in = new ObjectInputStream(this.socket.getInputStream());

            out.writeObject("Enter your name : ");
            clientName = (String) in.readObject();

            if(Server.currentClients.containsKey(clientName))
            {
                out.writeObject("Name already logged in. Connection closing.");
                socket.close();
                return;
            }
            else if(clientName==null || clientName.isEmpty())
            {
                out.writeObject("Name can not be null or empty . Connection closing.");
                socket.close();
                return;                
            }
            else
            {
                Server.currentClients.put(clientName,this);

                System.out.println("Client " + clientName + " connected.");

                if (Server.offlineClients.contains(clientName)) 
                {
                    Server.offlineClients.remove(clientName);
                    System.out.println("Client " + clientName + " is now online.");
                }


                createClientDirectory(clientName);
                logHistory("LOGIN", "LOGIN", "SUCCESS", clientName);
                out.writeObject("Welcome " + clientName + "! You are now connected succesfully to the server.");
            }

        while (true) 
        {
            Object obj=in.readObject();

            if(obj instanceof String )
            {
                String msg = (String)obj;
                System.out.println(msg);
            }
            else
            {

            int choice = (int)obj;
            getDoneForClientChoice(choice,clientName,out, in);
            }
        }

        }
        catch (IOException | ClassNotFoundException e) 
        {
            //e.printStackTrace();
            System.out.println("Connection lost unexpectedly from server with "+clientName);
            // if(clientName!=null)
            //     logHistory("Disconnection", "LOGOUT", "FAILED", clientName);
            // Server.currentClients.remove(clientName);
            if(clientName != null && Server.currentClients.containsKey(clientName))
            {
                logHistory("Disconnection", "LOGOUT", "FAILED", clientName);
            }
            Server.currentClients.remove(clientName);
            if(!Server.offlineClients.contains(clientName))
            {
                Server.offlineClients.add(clientName);
            }
        }
    }

    private void createClientDirectory(String clientName) 
    {
        File baseClientDirectory = new File("Server/ServerFiles/" + clientName);

        if (baseClientDirectory.exists()) {
            System.out.println("Directory already exists for client: " + clientName);
            return;
        }

        File publicDir = new File(baseClientDirectory, "Public");
        File privateDir = new File(baseClientDirectory, "Private");

        boolean successPublic = publicDir.mkdirs();
        boolean successPrivate = privateDir.mkdirs();

        if (successPublic && successPrivate) {
            System.out.println("Directory created for client: " + clientName);
        } else {
            System.out.println("Failed to create directories for client: " + clientName);
        }
    }


    private void getDoneForClientChoice(int choice ,String clientName , ObjectOutputStream out, ObjectInputStream in)
    {

        switch (choice) 
        {
            case 1:
                lookUpClientStatus(clientName,out);
                break;
            case 2:
                lookUpOwnFiles(clientName,out);
                break;
            case 3:
                downloadFile(clientName,out,in);
                break;
            case 4:
                lookUpOthersPublicFiles(clientName,out);
                break;
            case 5:
                requestFile(clientName,out,in);
                break;
            case 6:
                viewUnreadMessages(clientName);
                break;
            case 7:
                uploadFile(clientName, out , in);
                break;
            case 8:
                viewHistory(clientName);
                break;
            case 9:
                showRequestID(clientName,out);
                break;    
            case 0:
                logout(clientName);
                break;
            default:
                System.out.println("Invalid choice.");
        }
    }

    private void lookUpClientStatus(String clientName, ObjectOutputStream out)
    {
        try 
        {
            ArrayList<String> onlineClients=new ArrayList<>(Server.currentClients.keySet());
            ArrayList<String> offlineClients=new ArrayList<>(Server.offlineClients);
            
            String clientList="";


            for (String client : onlineClients) 
            {
                clientList+=(client + " (Online)\n");
            }
            for (String client : offlineClients) 
            {
                clientList+=(client + " (Offline)\n");
            }  
            out.writeObject(clientList);          
        }    
        catch( IOException e)
        {
            e.printStackTrace();
        }  

    }
    private void lookUpOwnFiles(String clientName, ObjectOutputStream out) 
    {
        try
        {

        String fileList = "";
        for (FileUploadInfo fui : Server.fileMap.values())
        {
            if (fui.clientName.equals(clientName))
            {

                fileList+=(fui.fileName+"------- ("+fui.accessType+")"+fui.fileId+"------- "+fui.fileSize+" Bytes");
            }
        }

        if (fileList.isEmpty())
        {
            out.writeObject("No files found for client " + clientName);
            return;
        }
        out.writeObject(fileList);

        }
        catch( IOException e)
        {
            e.printStackTrace();
        }
    }


    private void downloadFile(String clientName, ObjectOutputStream out, ObjectInputStream in) 
    {
        try
        {
        String fileId = (String) in.readObject();
        FileUploadInfo fui = Server.fileMap.get(fileId);
        if(fui == null)
        {
            out.writeObject("File has not been found because of invalid file ID.");
            logHistory("NULL","DOWNLOAD", "FAILED",clientName);
            return;
        }
        if(!fui.accessType.equalsIgnoreCase("Public") && !fui.clientName.equals(clientName))
        {

           out.writeObject("You do not have access to download this file . ");
           logHistory(fui.fileName,"DOWNLOAD", "FAILED",clientName);
           return;
        }

        //out.writeObject("This is valid file .");
        File fileToDownload = new File("Server/ServerFiles/" + fui.clientName + "/" + fui.accessType + "/" + fui.fileName);
        if(!fileToDownload.exists())
        {
            out.writeObject("File not found on server storage.");
            logHistory(fui.fileName,"DOWNLOAD", "FAILED",clientName);
            return;
        }

        out.writeObject("This is valid file .");
        out.writeObject(fui.fileName);

        FileInputStream fileinputstream = new FileInputStream(fileToDownload);
        byte[] buffer = new byte[Server.MAX_CHUNK_SIZE];
        int bytesRead = fileinputstream.read(buffer);
        while(bytesRead != -1)
        {
            byte[] fileBytes = new byte[bytesRead];  
            System.arraycopy(buffer, 0, fileBytes, 0, bytesRead);  
            out.writeObject(fileBytes);
            System.out.println("File chunk has been read : "+fileBytes);
            bytesRead = fileinputstream.read(buffer);
        }
        fileinputstream.close();
        //out.writeObject(fui.fileName);
        out.writeObject("File download completed .");
        logHistory(fui.fileName,"DOWNLOAD", "SUCCESS",clientName);
        System.out.println("File has been sent to "+clientName+" from the server.");
    
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    private void lookUpOthersPublicFiles(String clientName , ObjectOutputStream out)
    {
        try
        {
        String othersPublicFile = "";
        for(FileUploadInfo fui : Server.fileMap.values())
        {
            if(fui.clientName.equals(clientName))
            {
                continue;
            }
            if(fui.accessType.equalsIgnoreCase("Public"))
            {
                othersPublicFile+=(fui.fileName+"------- "+fui.fileId+"------- "+fui.fileSize+" Bytes ------- Client "+fui.clientName);
            }
        }
        if(othersPublicFile.isEmpty())
        {
            out.writeObject("No public files are available from other clients.");
            return;
        }
        out.writeObject(othersPublicFile);
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

    }
    private boolean checkRequest(String clientName,String shortDescription,String recipient)
    {
        for (FileRequest fr : Server.fileRequestMap.values()) {
            if (fr.clientName.equals(clientName) && fr.shortDescription.equalsIgnoreCase(shortDescription) && !fr.requestFulfilled && fr.recipientClientName.equals(recipient)) 
            {

                
                return false;
            }
        }
        return true;

    }

    private void requestFile(String clientName, ObjectOutputStream out,ObjectInputStream in) 
    {
        try{
        out.writeObject("Give Short Description : ");
        String shortDescription = (String)in.readObject();
        out.writeObject("Who is the recipient (ALL/Client Name): ");
        String recipient = (String) in.readObject();
        String requestedFileId = "REQ_FILE_"+System.currentTimeMillis();

        if(!checkRequest(clientName,shortDescription,recipient))
        {
            out.writeObject("A similar request already exists and is pending. File request failed .");
            return;
        }

        FileRequest filerequest = new FileRequest(requestedFileId , shortDescription, recipient , clientName);
        Server.fileRequestMap.put(requestedFileId,filerequest);
        logHistory("REQUEST ID : "+requestedFileId, "REQUEST", "SUCCESS", clientName);  // <-- ADD THIS

        if(recipient.equalsIgnoreCase("ALL"))
        {
            for(String onlineClient : Server.currentClients.keySet())
            {
                if(!onlineClient.equals(clientName))
                {
                    writeUnreadMessagesToFile(onlineClient, "New file is requested to " +onlineClient+"| upload file from " +clientName+"| through Request ID "+requestedFileId+"| Description "+shortDescription+"\n");  
                }
            }
            for(String offlineClient : Server.offlineClients)
            {
              
               writeUnreadMessagesToFile(offlineClient, "New file is requested to " +offlineClient+"| upload file from " +clientName+"| through Request ID "+requestedFileId+"| Description "+shortDescription+"\n");       
            }
        }
        else
        {
            if(Server.currentClients.containsKey(recipient))
            {
                writeUnreadMessagesToFile(recipient, "New file is requested to " +recipient+"| upload file from " +clientName+"| through Request ID "+requestedFileId+"| Description "+shortDescription+"\n");                
            }

        }
        System.out.println(clientName + " has requested ------ "+requestedFileId);

        out.writeObject("Request created successfully by "+clientName +" to "+recipient+" through "+requestedFileId);
        }
        catch (IOException | ClassNotFoundException e)
        {
            e.printStackTrace();
        }
    }


    private void writeUnreadMessagesToFile(String clientName,String message)
    {
        try
        {
            File messageFile = new File("Server/ServerFiles/"+clientName+"/unreadMessageFile.txt");
            FileWriter filewriter = new FileWriter(messageFile,true);
            filewriter.write(message);
            filewriter.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    private static void printFileRequestMap() {

        System.out.println("Total Requests: " + Server.fileRequestMap.size());
        
        if(Server.fileRequestMap.isEmpty()) {
            System.out.println("Map is EMPTY - No requests found");
        } else {
            for(Map.Entry<String, FileRequest> entry : Server.fileRequestMap.entrySet()) {
                String key = entry.getKey();
                FileRequest fr = entry.getValue();
                
                System.out.println("\n--- Request Entry ---");
                System.out.println("Map Key: [" + key + "]");
                System.out.println("Request ID: [" + fr.requestID + "]");
                System.out.println("Description: " + fr.shortDescription);
                System.out.println("Requester (clientName): " + fr.clientName);
                System.out.println("Recipient: " + fr.recipientClientName);
            }
        }

    }
    private void showRequestID(String clientName, ObjectOutputStream out) 
    {
        try {
            if (Server.fileRequestMap.isEmpty()) 
            {
                out.writeObject("No pending file requests available.");
                return;
            }
            String receiveTheRequest="";

            for (FileRequest fr : Server.fileRequestMap.values()) 
            {
                if (fr.recipientClientName.equals(clientName) || (fr.recipientClientName.equalsIgnoreCase("ALL") && !fr.clientName.equals(clientName)))
                {
                    receiveTheRequest+=(("Request ID: ")+(fr.requestID)+(" | Description: ")+(fr.shortDescription)+(" | Requester: ")+(fr.clientName)+(" | Fulfilled: ")+(fr.requestFulfilled ? "Yes" : "No") +"\n");

                }
            }

            out.writeObject(receiveTheRequest);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean checkUpload(String clientName,String fileName,String fileAccessType,String requestId)
    {
        for (FileUploadInfo fui : Server.fileMap.values()) {
            if (fui.clientName.equals(clientName) && fui.fileName.equalsIgnoreCase(fileName) && fui.accessType.equalsIgnoreCase(fileAccessType)) 
            {
                if (requestId == null && fui.requestId == null) 
                {
                    return false;
                }

                if (requestId != null && requestId.equals(fui.requestId)) 
                {
                    return false;
                }
            }
        }
        return true;

    }
    private static synchronized boolean allocatePossible(long size)
    {
        if (Server.usedBufferSize + size > Server.MAX_BUFFER_SIZE)
            return false;
        Server.usedBufferSize += size;
        return true;
    }

    private static synchronized void freeBuffer(long size) 
    {
        Server.usedBufferSize -= size;
    }
    private static synchronized void addBuffer(long size) 
    {
        Server.usedBufferSize += size;
    }    

    private void uploadFile(String clientName, ObjectOutputStream out, ObjectInputStream in)
    {
        
        printFileRequestMap();
        String fileId=null;
        String fileName=null;
        FileUploadInfo fileUploadInfo=null; 
        try
        {
            String msgFromClient = (String) in.readObject();
            System.out.println("From Client # " + msgFromClient);
            if(msgFromClient!=null && msgFromClient.contains("cancel"))
            {
                System.out.println("The upload of file from client "+clientName +" has been cancelled.");
                logHistory("NULL","UPLOAD", "FAILED",clientName);
                return;
            }
            fileName = (String) in.readObject();
            long fileSize = (long) in.readObject();
            String fileAccessType = (String) in.readObject();
            String requestId = (String) in.readObject();
            System.out.println("Client " + clientName + " wants to upload file: " + fileName + " of size: " + fileSize + " bytes as " + fileAccessType + " file with request ID: " + requestId);
           if (requestId != null && !requestId.isEmpty()) 
            {
                if (!Server.fileRequestMap.containsKey(requestId)) 
                {
                    System.out.println("The upload of file from client " + clientName + " has been cancelled. Request ID not found.");
                    out.writeObject("Invalid Request ID. Upload cancelled.");
                    logHistory(fileName, "UPLOAD", "FAILED", clientName);
                    return;
                }
                FileRequest fileRequest = Server.fileRequestMap.get(requestId);
                if (fileRequest.requestFulfilled) 
                {
                    System.out.println("The upload of file from client " + clientName + " has been cancelled. This requested file has already been uploaded. ");
                    out.writeObject("Same Requested file uploaded . Upload cancelled.");
                    logHistory(fileName, "UPLOAD", "FAILED", clientName);
                    return;
                }
                String recipient = fileRequest.recipientClientName;
                
                if (!recipient.equals(clientName) && !recipient.equalsIgnoreCase("ALL")) 
                {
                    System.out.println("The upload of file from client " + clientName + " has been cancelled. Not authorized for request " + requestId);
                    out.writeObject("You are not the recipient of this request. Upload cancelled.");
                    logHistory(fileName, "UPLOAD", "FAILED", clientName);
                    return;
                }
            }
            if(!checkUpload(clientName,fileName,fileAccessType,requestId))
            {
                System.out.println("The upload of file from client " + clientName + " has been cancelled. Same file has been uploaded . ");
                out.writeObject("Same file has been uploaded already. Upload cancelled.");
                logHistory(fileName, "UPLOAD", "FAILED", clientName);
                return;
            }
            //synchronized(Server.class)
            //{
            //long newNeededBufferSpace = Server.usedBufferSize + fileSize;
            //if (newNeededBufferSpace > Server.MAX_BUFFER_SIZE) 
            if(!allocatePossible(fileSize))
            {
                System.out.println("The upload of file from client "+clientName +" has been cancelled. Due to buffer overflow.");
                out.writeObject("Server buffer overflow  so Upload request rejected.");
                logHistory(fileName,"UPLOAD", "FAILED",clientName);
                return;
            } 
            //}
            
            
            //System.out.println("DEBUG1");
            int fileChunkSize = Server.MIN_CHUNK_SIZE + (int )(Math.random() * (Server.MAX_CHUNK_SIZE - Server.MIN_CHUNK_SIZE + 1));
            
            fileId = "file" + System.currentTimeMillis();

            fileUploadInfo = new FileUploadInfo(fileId, fileName, fileSize, fileAccessType, requestId, clientName); 
            Server.fileMap.put(fileId, fileUploadInfo);
            //System.out.println("DEBUG2");
            out.writeObject("Upload request accepted . You can start uploading the file.");
            out.writeObject(fileChunkSize);
            out.writeObject(fileId);
           // System.out.println("DEBUG :: usedd buffer size before upload : "+Server.usedBufferSize);


            while(true)
            {
                Object fileMsgFromClient = in.readObject();
                if(fileMsgFromClient instanceof String && ((String)fileMsgFromClient).contains("completed"))
                {
                    break;
                }
                if (!(fileMsgFromClient instanceof byte[])) {
                    System.out.println("The upload of file from client "+clientName +" has been cancelled. Due to not getting byte chunk.");
                    out.writeObject("Unexpected data received. Upload aborted.");
                    logHistory(fileName,"UPLOAD", "FAILED",clientName);
                    return;
                }                    
                byte[] chunk = (byte[])fileMsgFromClient;
                fileUploadInfo.chunksList.add(chunk);
                fileUploadInfo.totalBytesReceived+=chunk.length;
                //synchronized(Server.class){
                //Server.usedBufferSize+=chunk.length;
                addBuffer(chunk.length);
                //}
                out.writeObject("Chunk received.");
            }
            
            if(fileUploadInfo.fileSize == fileUploadInfo.totalBytesReceived && fileUploadInfo.saveFileToTheServerStorage())
            {
               // System.out.println("DEBUG :: usedd buffer size after upload : "+Server.usedBufferSize);
               // synchronized(Server.class){
                //Server.usedBufferSize -= fileUploadInfo.totalBytesReceived;
                freeBuffer(fileUploadInfo.totalBytesReceived);
                //}
                fileUploadInfo.chunksList.clear();
                out.writeObject("File uploaded successfully .");
                if(requestId != null)
                {
                    FileRequest filerequest=Server.fileRequestMap.get(requestId);
                    if(filerequest!=null)
                    {
                        filerequest.requestFulfilled=true;
                        filerequest.uploadedFileId=fileUploadInfo.fileId;
                        String responseFromClient = (String) in.readObject();
                        if(responseFromClient.trim().equalsIgnoreCase("notify request sender."))
                        {
                            notifyFileUploadedInRequestResponse(fileUploadInfo);
                        }  
                        Server.fileRequestMap.remove(requestId);                    

                    }
                }
                logHistory(fileName,"UPLOAD", "SUCCESS",clientName);
            }
            else
            {   System.out.println("Here");
                out.writeObject("File upload failed .");
                logHistory(fileName,"UPLOAD", "FAILED",clientName);  
                //synchronized(Server.class)
                //{
                //Server.usedBufferSize-=fileUploadInfo.totalBytesReceived;
                freeBuffer(fileUploadInfo.totalBytesReceived);
                //}
                Server.fileMap.remove(fileUploadInfo.fileId);
                
            }  
                

        }
        catch (IOException | ClassNotFoundException e)
        {
           // e.printStackTrace();

           System.out.println("File upload interrupted for client " + clientName + ". Cleaning up partial data.");
           if(fileId!=null)
           {
            FileUploadInfo fui = Server.fileMap.get(fileId);
            //synchronized(Server.class)
            //{
            if(fui!=null)
            {

            //Server.usedBufferSize-=fui.totalBytesReceived;
            freeBuffer(fui.totalBytesReceived);
            Server.fileMap.remove(fileId);
            System.out.println("Cleaned up partial data for file ID: " + fileId);
            }
           //}
           }
           Server.currentClients.remove(clientName);
           Server.offlineClients.add(clientName);
           logHistory(fileName,"UPLOAD", "FAILED",clientName);
        }

    }    



    private void notifyFileUploadedInRequestResponse(FileUploadInfo fileUploadInfo)
    {
        try 
        {

            String requestId = fileUploadInfo.requestId;
            FileRequest fr = Server.fileRequestMap.get(requestId);

            if(fr == null) 
                return;
            String requestSender = fr.clientName; 

            if(Server.currentClients.containsKey(requestSender))
            {
                Worker requestSenderWorker = Server.currentClients.get(requestSender);
                //requestSenderWorker.out.writeObject("A file you requested has been uploaded!" +"\nFile ID: " + fileUploadInfo.fileId  +"\nFile Name: " + fileUploadInfo.fileName +"\nUploaded By: " + fileUploadInfo.clientName);
                writeUnreadMessagesToFile(requestSenderWorker.clientName ,"A file you requested has been uploaded!" +"|File ID: " + fileUploadInfo.fileId  +"|File Name: " + fileUploadInfo.fileName +"|Uploaded By: " + fileUploadInfo.clientName+"\n");
            }
            else if(Server.offlineClients.contains(requestSender))
            {
               writeUnreadMessagesToFile(requestSender,"A file you requested has been uploaded!" +"|File ID: " + fileUploadInfo.fileId  +"|File Name: " + fileUploadInfo.fileName +"|Uploaded By: " + fileUploadInfo.clientName+"\n");
            }

            System.out.println("Notified request sender " + requestSender + " about file upload " + fileUploadInfo.fileId);

        } 
        catch (Exception e) 
        {
            System.out.println("Failed to notify request sender.");
        }
    }

    private void viewUnreadMessages(String clientName) 
    {
        
        try
        {
            File messageFile = new File("Server/ServerFiles/"+clientName+"/unreadMessageFile.txt");
            
            if(!messageFile.exists() || messageFile.length() == 0)
            {
                out.writeObject("No unread messages remaining . ");
                return;
            }
            FileInputStream fileinputstream = new FileInputStream(messageFile);
            String messages = "";
            ArrayList<Byte> messageBuffer = new ArrayList<>();

            int readByte=fileinputstream.read();
            while(readByte!=-1)
            {
                if(readByte == '\n')
                {
                    byte[] alineBytes =  new byte[messageBuffer.size()];

                    for(int i=0;i<messageBuffer.size();i++)
                    {
                        alineBytes[i]=messageBuffer.get(i);
                    }
                    messageBuffer.clear();
                    String aline=new String(alineBytes).trim();
                    if(!aline.isEmpty())
                    {
                        messages+=(aline);
                    }

                }
                else
                {
                    messageBuffer.add((byte)readByte);
                }
                readByte=fileinputstream.read();
            }
            fileinputstream.close();
            if(messages.isEmpty())
            {
                out.writeObject("No unread message for "+clientName);
            }
            else
            {
                out.writeObject(messages);
                FileOutputStream fileoutputsream = new FileOutputStream(messageFile);
                fileoutputsream.close();

            }


        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }    
    private void viewHistory(String clientName) 
    {
        try
        {
            File historyFile = new File("Server/ServerFiles/"+clientName+"/history.txt");
            
            if(!historyFile.exists() || historyFile.length() == 0)
            {
                out.writeObject("No unread messages remaining . ");
                return;
            }
            FileInputStream fileinputstream = new FileInputStream(historyFile);
            String histories ="";
            ArrayList<Byte> historyBuffer = new ArrayList<>();

            int readByte=fileinputstream.read();
            while(readByte!=-1)
            {
                if(readByte == '\n')
                {
                    byte[] alineBytes =  new byte[historyBuffer.size()];

                    for(int i=0;i<historyBuffer.size();i++)
                    {
                        alineBytes[i]=historyBuffer.get(i);
                    }
                    historyBuffer.clear();
                    String aline=new String(alineBytes).trim();
                    if(!aline.isEmpty())
                    {
                        histories+=(aline);
                    }

                }
                else
                {
                    historyBuffer.add((byte)readByte);
                }
                readByte=fileinputstream.read();
            }
            fileinputstream.close();
            if(histories.isEmpty())
            {
                out.writeObject("No history message for "+clientName);
            }
            else
            {
                out.writeObject(histories);
                // FileOutputStream fileoutputsream = new FileOutputStream(historyFile);
                // fileoutputsream.close();

            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

        
    }

    private void logHistory(String fileName,String operation,String status,String clientName)
    {
        try
        {
            File historyFile = new File("Server/ServerFiles/"+clientName+"/history.txt");
            
            FileWriter filewriter=new FileWriter(historyFile,true);
            LocalDateTime ldt = LocalDateTime.now();
            DateTimeFormatter dtf =  DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
            String fomattedDateTime= ldt.format(dtf);
            filewriter.write(fileName+"-----"+fomattedDateTime+"-----"+operation+"-----"+status+"\n");
            filewriter.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }
    private void logout(String clientName) 
    {
        try
         {
            Server.currentClients.remove(clientName);
            if(!Server.offlineClients.contains(clientName))
                Server.offlineClients.add(clientName);
            if(socket != null && !socket.isClosed())    
                socket.close();
            System.out.println("Client " + clientName + " has logged out.");
            logHistory("LOGOUT", "LOGOUT", "SUCCESS", clientName);
         }
         catch(IOException e) 
         {
            e.printStackTrace();
         } 
        
    }
    
}

 


