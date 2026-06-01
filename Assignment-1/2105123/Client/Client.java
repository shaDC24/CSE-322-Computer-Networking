package Client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static ObjectOutputStream out;
    public static ObjectInputStream  in;
    




    public static void main(String[] args) throws IOException, ClassNotFoundException {

        Socket socket = new Socket("localhost", 6666);

        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());

        String msgFromServer = (String) in.readObject();
        System.out.println("From Server # " + msgFromServer);

        Scanner scanner = new Scanner(System.in);
        String clientName = scanner.nextLine();
        out.writeObject(clientName);
        msgFromServer = (String) in.readObject();
        System.out.println("From Server # " + msgFromServer);        
        if(msgFromServer.contains("closing"))
        {
            socket.close();
            return;
        } 
        ClientUtil clientUtil = new ClientUtil("localhost", 6666,out, in, scanner , clientName);
        while (true)
        {
            clientUtil.showMenu();

        }    
        
    }
}
