package service;

import service.objects.Blob;
import service.objects.Tree;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;

public class RequestHandler {


    static private final File HERE = new File(".");

    public void init() throws IOException {
        Git.init(HERE);
        System.out.println("Initialized empty Git repository in " + HERE.getAbsolutePath() + "/.git/");
    }

    public void catFile(String hash) throws IOException, NoSuchAlgorithmException {
        final var git = Git.open(HERE);
        var blob = (Blob) git.getObject(hash);
        System.out.writeBytes(blob.data());
    }


    public void hashFile(String path) throws IOException, NoSuchAlgorithmException {
        final var git = Git.open(HERE);
        var bytes = Files.readAllBytes(new File(path).toPath());
        var blob = new Blob(bytes);
        var hash = git.writeOject(blob);
        System.out.println(hash);
    }

    public void lsTree(String hash) throws IOException, NoSuchAlgorithmException {
        final var git = Git.open(HERE);
        var tree = (Tree) git.getObject(hash);
        for (var entry : tree.entries()) {
            System.out.println(entry.name());
        }
    }

}
