import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

class MyComparator implements Comparator<MyClass> {
    @Override
    public int compare(MyClass o1, MyClass o2) {
        return o1.getName().compareTo(o2.getName());
    }
};

public class ApplyLambda {
    public static void main(String[] args) {

        // Runnable is a functional interface
        Runnable r1 = new Runnable() {
            public void run() {
                System.out.println("Runnable Interface 1");
            }
        };

        Thread t1 = new Thread(r1);
        t1.start();

        Runnable r2 = () -> System.out.println("Runnable Interface 2");

        Thread t2 = new Thread(r2);
        t2.start();

        Runnable r3 = () -> {
            for (int i = 0; i < 10; i++) {
                System.out.print("Runnable Interface 3 ");
            }
            System.out.println();
        };

        Thread t3 = new Thread(r3);
        t3.start();

        List<MyClass> al = new ArrayList<>();

        al.add(new MyClass(1, "C"));
        al.add(new MyClass(3, "A"));
        al.add(new MyClass(2, "E"));
        al.add(new MyClass(5, "B"));
        al.add(new MyClass(4, "D"));
        al.add(new MyClass(2, "F"));

        printList(al);
        Collections.sort(al, new MyComparator());
        printList(al);

        printList(al);
        // Comparator is a functional interface
        Collections.sort(al, new Comparator<MyClass>() {
            @Override
            public int compare(MyClass o1, MyClass o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        printList(al);

        printList(al);
        Collections.sort(al, (o1, o2) -> o1.getName().compareTo(o2.getName()));
        printList(al);

        printList(al);
        Collections.sort(al, (o1, o2) -> {
            if (o1.getId() == o2.getId()) {
                return o1.getName().compareTo(o2.getName());
            }
            return o1.getId() - o2.getId();
        });
        printList(al);

        printList(al);
        Collections.sort(al, Comparator.comparing(MyClass::getName));
        printList(al);
    }

    public static void printList(List<MyClass> list) {
        System.out.println("----------");
        list.forEach(e -> System.out.println(e.getId() + ", " + e.getName()));
    }
}
