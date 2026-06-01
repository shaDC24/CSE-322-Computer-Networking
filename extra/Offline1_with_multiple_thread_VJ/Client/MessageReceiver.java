package Client;
import java.io.*;
public class MessageReceiver implements Runnable 
{
    ObjectInputStream in;


    public MessageReceiver(ObjectInputStream in)
    {
        this.in=in;

    }
        

    public void run() {
        try 
        {
            while (true) 
            {
                
                String msgFromServer = (String) in.readObject();
                if (msgFromServer == null) 
                    break;
                
                System.out.println("\n[From Server # " + msgFromServer);
                System.out.print("Enter command: ");
            }
        } 
        catch (IOException | ClassNotFoundException e) 
        {
            System.out.println("Error receiving message: " + e.getMessage());
            System.out.println("\nConnection closed by server");
            System.exit(0);
        } 
    }
}