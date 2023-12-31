package service;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class RequestHandler {


    private final File HERE = new File(".");

    public void init() throws IOException {
        Git.init(HERE);
        System.out.println("Initialized empty Git repository in " + HERE.getAbsolutePath() + "/.git/");
    }

    public void catFile(String hash) throws IOException {
        final var git = Git.open(HERE);
        final var bytes = git.catFile(hash);
        System.out.writeBytes(bytes);
    }


    public void hashFile(String path) throws IOException, NoSuchAlgorithmException {
        final var git = Git.open(HERE);
        final var hash = git.hashFile(new File(path));
        System.out.println(hash);
    }

}
