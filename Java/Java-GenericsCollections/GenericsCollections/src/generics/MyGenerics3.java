package generics;

class X {
    int x;

    void f1() {
        System.out.println("In f1");
    }
}

interface Y {
    void f2();
}

interface Z {
    void f3();
}

interface W {
    void f4();
}

class MyX extends X implements Y, Z {
    public void f2() {
        System.out.println("In f2");
    }

    public void f3() {
        System.out.println("In f3");
    }
}

class MyY extends X implements Y, Z, W {
    public void f2() {
        System.out.println("In f2");
    }

    public void f3() {
        System.out.println("In f3");
    }

    public void f4() {
        System.out.println("In f4");
    }
}

public class MyGenerics3<T extends X & Y & Z> {
    private T a;

    public void setObj(T a) {
        this.a = a;
    }

    public T getObj() {
        return this.a;
    }

    public static void main(String[] args) {
        MyX objX = new MyX();
        MyGenerics3<MyX> myGenerics = new MyGenerics3<>();
        myGenerics.setObj(objX);
        System.out.println(myGenerics.getObj());
        MyY objY = new MyY();
        MyGenerics3<MyY> myGenerics2 = new MyGenerics3<>();
        myGenerics2.setObj(objY);
        System.out.println(myGenerics2.getObj());

    }
}