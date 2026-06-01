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
    HashMap<String,FileUpload>uploadFileMap;
    ArrayList<FileRequest> getRequestList;
 

    public Worker(Socket socket)
    {
        this.socket = socket;
        this.clientName=null;
        this.uploadFileMap=new HashMap<>();
        this.getRequestList=new ArrayList<>();

    }

    public void run()
    {
        
        try {
            out = new ObjectOutputStream(this.socket.getOutputStream());
            in = new ObjectInputStream(this.socket.getInputStream());

            sendMessage("Enter your name : ");
            clientName = (String) in.readObject();

            if(clientName==null || clientName.trim().isEmpty())
            {
                sendMessage("Null name given . Connection closing.");
                socket.close();
                return;
            }
            else if (!Server.loggedInClient(clientName.trim(),this))
            {
                sendMessage("Name already logged in. Connection closing.");
                socket.close();
                return;
            }
            else
            {
                System.out.println("Client " + clientName + " connected.");
                createClientDirectory(clientName);
                logHistory("LOGIN", "LOGIN", "SUCCESS", clientName);
                sendMessage("Welcome " + clientName + "! You are now connected succesfully to the server.");
            }

        while (true) 
        {
            Object obj=in.readObject();
            String choice=(String)obj;

           getDoneForClientChoice(choice,clientName,out, in);
        }

        }
        catch (IOException | ClassNotFoundException e) 
        {
            System.out.println("Connection lost unexpectedly from server with "+clientName);
            logout();
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


    private void getDoneForClientChoice(String choice ,String clientName , ObjectOutputStream out, ObjectInputStream in)throws IOException
    {

        switch (choice) 
        {
            case "1":
                lookUpClientStatus(clientName,out);
                break;
            case "2":
                lookUpOwnFiles(clientName,out);
                break;
            case "3":
                downloadFile(clientName,out,in);
                break;
            case "4":
                lookUpOthersPublicFiles(clientName,out);
                break;
            case "5":
                requestFile(clientName,out,in);
                break;
            case "6":
                viewUnreadMessages(clientName);
                break;
            case "7":
                uploadFile(clientName, out , in);
                break;
            case "8":
                viewHistory(clientName);
                break;
            case "0":
                logout();
                break;
            default:
                System.out.println("Invalid choice.");
        }
    }

    private void lookUpClientStatus(String clientName, ObjectOutputStream out)
    {
        
            ArrayList<String> onlineClients=Server.getOnlineClients();
            ArrayList<String> offlineClients=Server.getOfflineClients();
            String allnames="";

            for (String client : onlineClients) 
            {
                allnames+=(client + " (Online)\n");
            }
            for (String client : offlineClients) 
            {
                allnames+=(client + " (Offline)\n");
            }  
            sendMessage(allnames);          


    }
    private void lookUpOwnFiles(String clientName, ObjectOutputStream out) 
    {

    }


    private void downloadFile(String clientName, ObjectOutputStream out, ObjectInputStream in) 
    {

    }

    private void lookUpOthersPublicFiles(String clientName , ObjectOutputStream out)
    {


    }

    private void requestFile(String clientName, ObjectOutputStream out,ObjectInputStream in) 
    {

    }


    private void writeUnreadMessagesToFile(String clientName,String message)
    {

    }

    public static void printFileRequestMap() {

    }

    private void uploadFile(String clientName, ObjectOutputStream out, ObjectInputStream in) 
    {
        try
        {
        String request=(String)in.readObject();
        String[] parts = request.split("|", 2);
        for(int i=0;i<parts.length;i++)
        {
            System.out.println(parts[i]);
        }
        }
        catch(IOException | ClassNotFoundException e)
        {
            e.printStackTrace();
        }


    }



    private void viewUnreadMessages(String clientName) 
    {
        
        /*try
        {
            File messageFile = new File("Server/ServerFiles/"+clientName+"/unreadMessageFile.txt");
            
            if(!messageFile.exists() || messageFile.length() == 0)
            {
                out.writeObject("No unread messages remaining . ");
                return;
            }
            FileInputStream fileinputstream = new FileInputStream(messageFile);
            ArrayList<String> messages = new ArrayList<>();
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
                        messages.add(aline);
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
        }*/
    }    
    private void viewHistory(String clientName) 
    {
        
    }

    private void logHistory(String fileName,String operation,String status,String clientName)
    {

    }
    private void logout() 
    {
        try
         {
            Server.loggedOutClient(clientName);
            if(socket != null && !socket.isClosed())    
                socket.close();
            for(FileUpload fu : uploadFileMap.values())
            {
                System.out.println("Yet to be implemented");
                 
            } 
            uploadFileMap.clear();   
            System.out.println("Client " + clientName + " has logged out.");
            logHistory("LOGOUT", "LOGOUT", "SUCCESS", clientName);
         }
         catch(IOException e) 
         {
            e.printStackTrace();
         } 
        
    }

    public void sendMessage(String message) 
    {
        try 
        {
            System.out.println("Server is sending : "+message);
            out.writeObject(message);

        } 
        catch (IOException e) 
        {
            System.out.println("Error sending message to " + clientName + ": " + e.getMessage());
        }
    }
    
}

 


