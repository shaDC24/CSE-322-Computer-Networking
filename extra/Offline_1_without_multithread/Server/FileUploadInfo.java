package Server;


import java.util.ArrayList;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.io.FileOutputStream;


public class FileUploadInfo implements Serializable
{
    public String fileName;
    public long fileSize;
    public String accessType; 
    public String requestId;
    public String fileId;
    public String clientName;
    public ArrayList<byte[]> chunksList;
    public long totalBytesReceived ;
    public boolean isComplete;


    public FileUploadInfo(String fileId, String fileName, long fileSize, String accessType, String requestId , String clientName) 
    {
        this.fileId = fileId;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.accessType = accessType;
        this.requestId = requestId;
        this.clientName = clientName;
        this.chunksList = new ArrayList<>();
        this.totalBytesReceived = 0;    
        this.isComplete = false;    
    }
    public boolean saveFileToTheServerStorage() throws IOException
    {
        String serverStoragePath = "Server/ServerFiles/"+clientName+"/"+accessType;
        File folder = new File(serverStoragePath);
        if(!folder.exists())
        {
            
            System.out.println("File can not be saved .");
            return false;
        }
        else
        {
            File newOutputFile = new File(folder,fileName);
            FileOutputStream file = new  FileOutputStream(newOutputFile);

            for(byte[] chunk : chunksList)
            {
                file.write(chunk);
            }
            file.close();

            this.isComplete = true;
            System.out.println("Saved "+ fileName + " successfully.");


            try(FileWriter fileWriter = new FileWriter(Server.fileMapStorage, true))
            {
            
                String metadataLine = fileId + "|" + fileName + "|" + fileSize + "|" + accessType + "|" + requestId + "|" + clientName + "|" + totalBytesReceived + "\n";
                fileWriter.write(metadataLine);
                fileWriter.flush();
                fileWriter.close();
            }
            catch(IOException e)
            {
                System.out.println(e);
                return false;
            }
            return true;
        }


        
    }
}