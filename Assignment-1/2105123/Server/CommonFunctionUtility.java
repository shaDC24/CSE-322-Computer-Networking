package Server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;


public class CommonFunctionUtility
{
    public static ArrayList<ArrayList<String>> fileToHashMap(File file)
    {
        ArrayList<ArrayList<String>> contents = new ArrayList<>();
        if(!file.exists())
        {
            System.out.println("No file found.");
            return contents;
        }

        try
        {
            FileInputStream fileinputstream = new FileInputStream(file);
            ArrayList<Byte> bufferList = new ArrayList<>();
            int byteRead = fileinputstream.read();

            while(byteRead!=-1)
            {
                if(byteRead == '\n')
                {
                    byte[] bytesInaLine = new byte[bufferList.size()];
                    for(int i=0;i<bufferList.size();i++)
                    {
                        bytesInaLine[i]=bufferList.get(i);
                    }
                    bufferList.clear();
                    String line = new String(bytesInaLine).trim();
                    if(!line.isEmpty())
                    {
                        String[] parts = line.split("\\|");
                        ArrayList<String> oneLine = new ArrayList<>();
                        for(String part : parts)
                        {
                            oneLine.add(part.trim());
                        }
                        contents.add(oneLine);
                    }

                }
                else
                {
                    bufferList.add((byte)byteRead);
                }
                byteRead = fileinputstream.read();
            }

           // System.out.println("DEBUG :: File parsed successfully");

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return contents;
    }
}