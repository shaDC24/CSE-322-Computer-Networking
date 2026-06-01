package charstream;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

class BufferedReaderDemoWithException {
    public static void main(String args[]) {
        // BufferedReader br = null;
        // try {
        // br = new BufferedReader(new FileReader("files/TestFile.java"));
        // while (true) {
        // String s = br.readLine();
        // if (s == null) break;
        // System.out.println(s);
        // }
        // br.close();
        // } catch(IOException e) {
        // e.printStackTrace();
        // } finally {
        // try {
        // if (br!=null) br.close();
        // } catch(IOException e) {
        // e.printStackTrace();
        // }
        // }

        try (BufferedReader br = new BufferedReader(new FileReader("files/TestFile.java"))) {
            while (true) {
                String s = br.readLine();
                if (s == null)
                    break;
                System.out.println(s);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}