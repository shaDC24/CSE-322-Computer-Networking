public class StringTest {
    public static void main(String[] args) {
        String s = "Hello World";
        System.out.println(s.indexOf('o'));//4
        System.out.println(s.lastIndexOf('o'));//7
        char  c1 = s.charAt(6);
        System.out.println(c1);//W        
        char [] c2 = new char[5];
        s.getChars(0, 5, c2, 0);
        System.out.println(c2);//Hello
        String t = "hello ";
        System.out.println(s.regionMatches(true, 0, t, 0, 6)); //true
        System.out.println(s.startsWith("Hello")); //true
        System.out.println(s.endsWith("World")); //true
    }
}
