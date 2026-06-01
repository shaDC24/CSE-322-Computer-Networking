import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.Scanner;
import java.io.IOException;


public class Client {
    public static void main(String[] args) throws IOException , ClassNotFoundException{

        Socket socket=new Socket("127.0.0.1",6666);
        System.out.println("Client started...");

        
        ObjectOutputStream objectOutputStream=new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream objectInputStream=new ObjectInputStream(socket.getInputStream());

        Scanner scanner=new Scanner(System.in);
        String msg=scanner.nextLine();
        objectOutputStream.writeObject(msg);

        try{

        String msgFromServer=(String)objectInputStream.readObject();
        System.out.println("Message from server: "+ msgFromServer);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }


    }
}