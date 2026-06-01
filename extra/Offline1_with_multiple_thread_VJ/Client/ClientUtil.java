package Client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;
import java.util.ArrayList;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.Serializable;



public class ClientUtil implements Serializable
{
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Scanner scanner;
    private String clientName;

    public ClientUtil(ObjectOutputStream out, ObjectInputStream in, Scanner scanner, String clientName) {
        this.out = out;
        this.in = in;
        this.scanner = scanner;
        this.clientName = clientName;

    }

    public void showMenu() throws IOException , ClassNotFoundException
    {
        System.out.println("Menu:");
        System.out.println("1. Look-up Client Status (online/offline)");
        System.out.println("2. Look-up uploaded files by Client (public/private)");
        System.out.println("3. Download file");
        System.out.println("4. Look-up uploaded files by other Client (public)");
        System.out.println("5. Request a file");
        System.out.println("6. View unread messages");
        System.out.println("7. Upload a file");
        System.out.println("8. View history");
        System.out.println("0. Log-out");


        System.out.print("Enter your choice: ");
        String choice =scanner.nextLine();


        if(choice.equals("1") || choice.equals("2"))
        {
            Sendcmd(choice);
        }
        else if(choice.equals("7"))
        {
            uploadFile();
        }
        else if(choice=="0")
        {
            System.out.println("Logging out...");
            System.exit(0);
        }
     


    }
    private void uploadFile() throws IOException , ClassNotFoundException
     {

        SampleFileUtil.createSampleFiles();

        String fileAccessType=null;

        System.out.println("\nIs this file to be uploaded as a request file?");
        System.out.println("1. Yes");
        System.out.println("0. No");
        System.out.print("Enter your choice (1/0): ");
        int responseChoice = Integer.parseInt(scanner.nextLine());  
        if (responseChoice != 0 && responseChoice != 1) {
            System.out.println("Invalid request choice!");
            Sendcmd("Upload cancel .");
            return;
        }    

        String requestId = null;
        if (responseChoice == 1) 
        {
            fileAccessType = "Public";
            System.out.print("Enter the Request ID: ");
            requestId = scanner.nextLine().trim();
            System.out.println("DEBUG :: "+requestId);

        }

        System.out.println("Upload File Options:");
        System.out.println("1. Enter file path manually");
        System.out.println("0. Use sample file Gui");

        System.out.print("Enter choice (1/0): ");
        int choiceForFile = Integer.parseInt(scanner.nextLine());
        File file = null;
        if (choiceForFile == 1) 
        {
            System.out.print("Enter the file path: ");
            String filePathName = scanner.nextLine();
            file = new File(filePathName);
            if (!file.exists()) 
            {
                System.out.println("File not found!");
                Sendcmd("Upload cancel .");
                return;
            }
        }
        else if (choiceForFile == 0) 
        {
            file = SampleFileUtil.chooseSampleFile(scanner);
            if (!file.exists()) 
            {
                System.out.println("Sample file not found!");
                Sendcmd("Upload cancel .");
                return;
            }
        } 
        else 
        {
            System.out.println("Invalid choice!");
            Sendcmd("Upload cancel .");
            return;
        }
        if(responseChoice == 0)
        {
            System.out.println("\nChoose access type:");
            System.out.println("1. Public");
            System.out.println("0. Private");
            System.out.print("Enter your choice (1/0): ");
            int fileAccessChoice = Integer.parseInt(scanner.nextLine());
            if (fileAccessChoice != 0 && fileAccessChoice != 1) 
            {
                System.out.println("Invalid access choice!");
                Sendcmd("Upload cancel .");
                return;
            }
            fileAccessType = (fileAccessChoice == 1) ? "Public" : "Private";
        }
        long fileSize = file.length();
        Sendcmd("Client "+clientName+" wants to upload a file|"+file.getName()+"|"+fileSize+"|"+fileAccessType+"|"+requestId);


    }





    private void Sendcmd(String msg) throws IOException
    {
        try
        {
            out.writeObject(msg);
            System.out.println(clientName+" send message # "+msg);

        }
        catch(IOException e)
        {
            System.out.println("Client gets error when sending command "+e.getMessage());
        }
    }


 

}