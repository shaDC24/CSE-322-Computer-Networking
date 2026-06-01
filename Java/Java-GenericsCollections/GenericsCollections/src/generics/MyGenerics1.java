package generics;

public class MyGenerics1<T> {
    private T a;

    public void setObj(T a) {
        this.a = a;
    }

    public T getObj() {
        return this.a;
    }

    public static void main(String[] args) {
        MyGenerics1<Integer> myGenerics = new MyGenerics1<>();
        // var myGenerics2 = new MyGenerics1<Integer>();
        myGenerics.setObj(10);
        // myGenerics.setObj("Hello"); // error
        System.out.println(myGenerics.getObj());
        MyGenerics1<String> myGenerics1 = new MyGenerics1<>();
        myGenerics1.setObj("Hello");
        // myGenerics1.setObj(10); // error
        String str = myGenerics1.getObj();
        System.out.println(str);
        MyGenerics1 noGenerics = new MyGenerics1(); // still possible
        noGenerics.setObj(20);
        noGenerics.setObj("World");
        System.out.println(noGenerics.getObj());
    }
}
