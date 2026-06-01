package bytestream;

import java.io.Serializable;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

class MyClass implements Serializable {
    String s;
    int i;
    double d;

    public MyClass(String s, int i, double d) {
        this.s = s;
        this.i = i;
        this.d = d;
    }

    public String toString() {
        return "s=" + s + "; i=" + i + "; d=" + d;
    }
}

public class ObjectSerializationDemo {
    public static void main(String args[]) {

        System.out.println("Object Deserialization");
        try {
            MyClass object1 = new MyClass("Hello World", 100, 12.34);
            MyClass object2 = new MyClass("Hello World 2", 500, 56.78);
            System.out.println("object1: " + object1);
            System.out.println("object2: " + object2);
            FileOutputStream fos = new FileOutputStream("files/serial");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(object1);
            oos.writeObject(object2);
            oos.close();
        } catch (Exception e) {
            System.out.println("Exception during serialization: " + e);
            System.exit(0);
        }

        System.out.println("Object Serialization");
        try {
            MyClass object1, object2;
            FileInputStream fis = new FileInputStream("files/serial");
            ObjectInputStream ois = new ObjectInputStream(fis);
            object1 = (MyClass) ois.readObject();
            System.out.println("object1: " + object1);
            object2 = (MyClass) ois.readObject();
            System.out.println("object2: " + object2);
            ois.close();
        } catch (Exception e) {
            System.out.println("Exception during deserialization: " + e);
            System.exit(0);
        }
    }
}
