package Client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    Socket socket;
    ObjectOutputStream out;
    ObjectInputStream in;
    String clientName;

    public Client(String localHost,int port) throws IOException
    {
        this.socket = new Socket(localHost,port);
        System.out.println("Connection established to server: " + localHost + ":" +port);
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
    }
    

    public static void main(String[] args) throws IOException, ClassNotFoundException
     {
        Client client=new Client("localhost",6666);
        client.run();

    }

    public void run()throws IOException, ClassNotFoundException
    {
        Scanner scanner=new Scanner(System.in);
        String msgFromServer=(String)in.readObject();
        System.out.print("From Server # "+msgFromServer);
        this.clientName=scanner.nextLine().trim();

        if(clientName.isEmpty())
        {
            System.out.println("Name can not be empty.");
            socket.close();
            return;

        }
        out.writeObject(clientName);
        msgFromServer=(String)in.readObject();
        if(!msgFromServer.contains("succesfully"))
        {
            System.out.println("From Server # "+msgFromServer);
            socket.close();
            return;
           
        }
        ClientUtil clientUtil = new ClientUtil(out,in,scanner,clientName);

        Thread msgReceiveThread=new Thread(new MessageReceiver(in));
        msgReceiveThread.start();
        while(true)
        {
        clientUtil.showMenu();
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        }
    }
}
