package Server;
import java.io.Serializable;


public class FileRequest implements Serializable
{
    public String requestID;
    public String shortDescription;
    public String recipientClientName;
    public String clientName;

    public FileRequest(String requestID , String shortDescription, String recipientClientName , String clientName)
    {
        this.requestID=requestID;
        this.shortDescription=shortDescription;
        this.recipientClientName=recipientClientName;
        this.clientName=clientName;
    }
    


}