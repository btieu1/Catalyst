import java.io.*;

public class CreateFile {

    public static void main(String [] args) throws IOException {
        String input = "This is a test";
        try (FileOutputStream out = new FileOutputStream("output.txt")) {
            byte[] data = input.getBytes();
            out.write(data);
        } finally {
            System.out.println(input);
        }
    }
}
