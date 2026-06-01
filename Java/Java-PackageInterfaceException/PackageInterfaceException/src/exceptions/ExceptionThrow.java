package exceptions;

import java.io.IOException;

public class ExceptionThrow {
    public static void f() throws IOException {
        /*try {
            throw new NullPointerException("This is my custom generated exception");
        } catch (Exception e) {
            System.out.println("Inside catch of f()");
            throw e; //rethrow the exception
        }*/
        throw new NullPointerException("This is my custom generated exception"); 
        //throw new IOException("This is my custom generated exception"); 
    }

    public static void main(String args[]) {
        try {
            f();
        } catch (NullPointerException | IOException e) {
            System.out.println("Inside catch of main()");
            System.out.println(e);
        }
    }
}
	