package Server;
import java.io.Serializable;
import java.util.ArrayList;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileOutputStream;

public class FileRequest implements Serializable
{
    public String requestID;
    public String shortDescription;
    public String recipientClientName;
    public String clientName;

    public boolean requestFulfilled;
    public String uploadedFileId;


    public FileRequest(String requestID , String shortDescription, String recipientClientName , String clientName)
    {
        this.requestID=requestID;
        this.shortDescription=shortDescription;
        this.recipientClientName=recipientClientName;
        this.clientName=clientName;
        this.requestFulfilled=false;
        this.uploadedFileId=null;
    }
    

}