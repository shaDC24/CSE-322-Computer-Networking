package Client;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.ArrayList;
import java.io.FileOutputStream;


public class SampleFileUtil
{


    public static void createSampleFiles() throws IOException
    {

        File folder = new File("../sample_files");
        if (!folder.exists()) 
        {
            folder.mkdir();
        }

        String[] sampleFileNames = {"sample1.txt", "sample2.txt", "sample3.txt"};

        String[] sampleFileContents = 
        {
                "This is sample file 1.",
                "This is sample file 2.\nHello World!\nHi there, this is a test file.",
                "This is sample file 3.\nThis is socket programming assignment."
        };

        for (int i = 0; i < sampleFileNames.length; i++) 
        {
            File file = new File(folder, sampleFileNames[i]);

            if (!file.exists()) 
            {
                FileWriter writer = new FileWriter(file);
                writer.write(sampleFileContents[i]);
                writer.close();
            }
        }
    }

    public static File chooseSampleFile(Scanner scanner) 
    {
        File folder = new File("../sample_files");
        File[] files = folder.listFiles();
        if (files == null || files.length == 0) 
        {
            System.out.println("No sample files available!");
            return null;
        }

        System.out.println("\nAvailable sample files:");
        for (int i = 0; i < files.length; i++) 
        {
            System.out.println(i + ": " + files[i].getName());
        }



        System.out.print("Choose a sample file (enter number): ");
        int choice = Integer.parseInt(scanner.nextLine());

        if (choice < 0 || choice >= files.length) 
        {
            System.out.println("Invalid choice!");
            return null;
        }

        return files[choice];
    }
}
