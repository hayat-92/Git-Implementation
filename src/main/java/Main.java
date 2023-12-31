import service.RequestHandler;

import java.io.IOException;
import java.util.Arrays;

public class Main {

    static RequestHandler handler = new RequestHandler();

    public static void main(String[] args) throws IOException {
        System.out.println("Hello, world!************ " + Arrays.toString(args));
        final var command = args[0];
        switch (command) {
            case "init" -> handler.init();
            case "cat-file" -> handler.catFile(args[2]);
            default -> System.out.println("Unknown command: " + command);
        }
    }
}
