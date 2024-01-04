package service;

import service.objects.Blob;
import service.objects.Tree;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;

public class RequestHandler {


    static private final Path HERE = Paths.get(".");

    public void init() throws IOException {
        Git.init(HERE);
        System.out.println("Initialized empty Git repository in " + HERE.toAbsolutePath());
    }

    public void catFile(String hash) throws IOException, NoSuchAlgorithmException {
        final var git = Git.open(HERE);
        var blob = (Blob) git.readObject(hash);
        System.out.writeBytes(blob.data());
    }


    public void hashFile(String path) throws IOException, NoSuchAlgorithmException {
        final var git = Git.open(HERE);
        var hash = git.writeBlob(Paths.get(path));
        System.out.println(hash);
    }

    public void lsTree(String hash) throws IOException, NoSuchAlgorithmException {
        final var git = Git.open(HERE);
        var tree = (Tree) git.readObject(hash);
        for (var entry : tree.entries()) {
            System.out.println(entry.name());
        }
    }

    public void writeTree() throws IOException, NoSuchAlgorithmException {
        final var git = Git.open(HERE);
        var hash = git.writeTree(HERE);
        System.out.println(hash);
    }

}
