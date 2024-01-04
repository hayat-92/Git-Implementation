import service.RequestHandler;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class Main {

    static RequestHandler handler = new RequestHandler();

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        final var command = args[0];
        switch (command) {
            case "init" -> handler.init();
            case "cat-file" -> handler.catFile(args[2]);
            case "hash-object" -> handler.hashFile(args[2]);
            case "ls-tree" -> handler.lsTree(args[2]);
            case "write-tree" -> handler.writeTree();
            default -> System.out.println("Unknown command: " + command);
        }
    }
}
