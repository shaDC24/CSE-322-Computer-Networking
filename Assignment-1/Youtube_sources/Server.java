import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;



public class Server {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(6666);
        System.out.println("Server started...");

        while(true)
        {
            Socket socket = serverSocket.accept();
            System.out.println("Client connected...");


            
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());

            try
            {

                Object msg=objectInputStream.readObject();
                System.out.println("Message received from client: " + (String)msg);
                String serverResponse=(String)msg;
                serverResponse=serverResponse.toUpperCase();
                objectOutputStream.writeObject(serverResponse);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }


    }
}