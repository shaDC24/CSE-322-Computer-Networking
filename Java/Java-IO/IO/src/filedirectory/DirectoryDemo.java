package filedirectory;

import java.io.File;

class DirectoryDemo {
    public static void main(String args[]) {
        String dirname = "files/dir";
        File f = new File(dirname);
        visit(f);
    }

    public static void visit(File f) {
        System.out.println(f.getName());
        if (f.isDirectory()) {
            File files[] = f.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    visit(files[i]);
                } else {
                    System.out.println(files[i].getName());
                }
            }
        } else {
            System.out.println(f.getName());
        }
    }
}
