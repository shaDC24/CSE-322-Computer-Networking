package p1;

public class SamePackage {
    SamePackage() {
        System.out.println("---SamePackage---");
        Protection p = new Protection();
        System.out.println(p.n);
        // System.out.println(p.nPrivate); //nPrivate has private access in
        // p1.Protection
        System.out.println(p.nProtected);
        System.out.println(p.nPublic);
    }

    public static void main(String[] args) {
        new Protection();
        new DerivedProtection();
        new SamePackage();
    }
}
