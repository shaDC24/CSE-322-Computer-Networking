
public class WrapDemo {
    public static void main(String args[]) {
        Integer iOb = new Integer(100); // boxing
        int i = iOb.intValue(); // unboxing
        System.out.println(i + " " + iOb);
        Integer jOb = 200; // auto boxing
        int j = jOb; // auto unboxing
        System.out.println(j + " " + jOb);
    }
}