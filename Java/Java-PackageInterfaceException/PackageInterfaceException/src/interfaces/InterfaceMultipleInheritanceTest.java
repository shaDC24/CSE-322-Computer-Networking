package interfaces;

interface Alpha {
    default void reset() {
        System.out.println("Alpha's reset");
    }
}

interface Beta {
    default void reset() {
        System.out.println("Beta's reset");
    }
}

public class InterfaceMultipleInheritanceTest implements Alpha, Beta {
    public void reset() {
        System.out.println("Beta's reset");
    }
}
