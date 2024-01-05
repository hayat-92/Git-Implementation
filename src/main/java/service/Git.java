package service;

import service.author.AuthorSignature;
import service.entry.TreeEntry;
import service.mode.TreeEntryMode;
import service.objects.Blob;
import service.objects.Commit;
import service.objects.Tree;
import service.platform.Platform;
import service.types.ObjectType;

import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;

import java.io.*;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;
import java.nio.file.Files;

public class Git {

    private static final byte[] OBJECT_TYPE_BLOB = "blob".getBytes();
    public static HexFormat HEX = HexFormat.of();
    public static final Set<Path> FORBIDDEN_DIRS = Set.of(
            Paths.get(".git")
    );
    private static final byte[] SPACE = " ".getBytes();
    private static final byte[] NULL = {0};
    private final Path root;

    Git(Path root) {
        this.root = root;
    }

    public Path getDotGit() {
        return root.resolve(".git");
    }

    public Path getObjectsDirectory() {
        return getDotGit().resolve("objects");
    }

    public Path getRefsDirectory() {
        return getDotGit().resolve("refs");
    }

    public Path getHeadFile() {
        return getDotGit().resolve("HEAD");
    }

    public Path getConfigFile() {
        return getDotGit().resolve("config");
    }


    public static Git init(Path root) throws IOException {
        var git = new Git(root);
        var dotGit = git.getDotGit();
        if (Files.exists(dotGit)) {
            throw new FileAlreadyExistsException(dotGit.toString());
        }

        Files.createDirectories(git.getObjectsDirectory());
        Files.createDirectories(git.getRefsDirectory());

        var head = git.getHeadFile();
        Files.createFile(head);
        Files.write(head, "ref: refs/heads/master\n".getBytes());

        var config = git.getConfigFile();
        Files.createFile(config);
        Files.write(config, "[core]\n\trepositoryformatversion = 0\n\tfilemode = false\n\tbare = false\n\tlogallrefupdates = false\n".getBytes());

        return git;
    }


    public static Git open(Path root) throws IOException {
        var git = new Git(root);
        var dotGit = git.getDotGit();
        if (!Files.exists(dotGit)) {
            throw new FileNotFoundException(dotGit.toString());
        }

        return git;
    }

    public String writeOject(service.objects.Object object) throws IOException, NoSuchAlgorithmException {
        var objectType = ObjectType.byClass(object.getClass());
        var objectTypeBytes = objectType.getName().getBytes();
        var tempPath = Files.createTempFile("temp-", ".temp");

        try {
            try (
                    var outputStream = Files.newOutputStream(tempPath);
                    var dataOutputStream = new DataOutputStream(outputStream);
            ) {
                objectType.serialize(object, dataOutputStream);
            }

            var length = Files.size(tempPath);
            var lengthBytes = String.valueOf(length).getBytes();

            var message = MessageDigest.getInstance("SHA-1");
            message.update(objectTypeBytes);
            message.update(SPACE);
            message.update(lengthBytes);
            message.update(NULL);

            try (
                    var inputStream = Files.newInputStream(tempPath);
            ) {
                var buffer = new byte[1024];
                int read;
                while ((read = inputStream.read(buffer)) != -1) {
                    message.update(buffer, 0, read);
                }
            }


            var hashBytes = message.digest();
            var hash = HexFormat.of().formatHex(hashBytes);
            var firstTwo = hash.substring(0, 2);
            var firstTwoDirectory = new File(getObjectsDirectory().toFile(), firstTwo);
            firstTwoDirectory.mkdirs();
            var rest = hash.substring(2);
            var restFile = new File(firstTwoDirectory, rest);

            try (
                    var outputStream = Files.newOutputStream(restFile.toPath());
                    var deflaterOutputStream = new DeflaterOutputStream(outputStream);
                    var inputStream = Files.newInputStream(tempPath);
            ) {
                deflaterOutputStream.write(objectTypeBytes);
                deflaterOutputStream.write(SPACE);
                deflaterOutputStream.write(lengthBytes);
                deflaterOutputStream.write(NULL);
                inputStream.transferTo(deflaterOutputStream);
            }

            return hash;
        } finally {
            Files.deleteIfExists(tempPath);
        }


    }

    public service.objects.Object readObject(String hash) throws IOException, NoSuchAlgorithmException {
        var firstTwo = hash.substring(0, 2);
        var rest = hash.substring(2);

        var path = getObjectsDirectory().resolve(firstTwo).resolve(rest);

        try (
                var inputStream = new FileInputStream(path.toFile());
                var inflaterInputStream = new InflaterInputStream(inputStream);
        ) {
            var builder = new StringBuilder();

            int value;
            while ((value = inflaterInputStream.read()) != -1 && value != ' ') {
                builder.append((char) value);
            }

            var type = ObjectType.byName(builder.toString());

            builder = new StringBuilder();
            while ((value = inflaterInputStream.read()) != -1 && value != 0) {
                builder.append((char) value);
            }

            var objectLength = Integer.parseInt(builder.toString());
            return type.deserialize(new DataInputStream(inflaterInputStream));
        }
    }

    public String writeBlob(Path path) throws IOException, NoSuchAlgorithmException {
        var bytes = Files.readAllBytes(path);
        var blob = new Blob(bytes);
        return writeOject(blob);

    }

    public String writeTree(Path root) throws IOException, NoSuchAlgorithmException {
        var file = Files.list(root);
        var filenames = file.map(Path::getFileName);
        var filteredFileNames = filenames.filter(filename -> !FORBIDDEN_DIRS.contains(filename)).toList();

        var entries = new ArrayList<TreeEntry>();
        for (var filename : filteredFileNames) {
            var path = root.resolve(filename);
            String hashString;
            TreeEntryMode mode;
            if (Files.isDirectory(path)) {
                hashString = writeTree(path);
                mode = TreeEntryMode.directory();
            } else if (Files.isRegularFile(path)) {
                hashString = writeBlob(path);
                if (Platform.isWindows()) {
                    mode = TreeEntryMode.regularFile(0644);
                } else {
                    var attributes = Files.readAttributes(path, PosixFileAttributes.class);
                    mode = TreeEntryMode.regularFile(attributes);
                }
            } else {
                continue;
            }
            var hash = HEX.parseHex(hashString);
            entries.add(new TreeEntry(mode, filename.toString(), hash));
        }
        Collections.sort(entries);
        var tree = new Tree(entries);
        return writeOject(tree);
    }

    public String writeCommit(String treeHash, String parentHash, AuthorSignature author, String message) throws IOException, NoSuchAlgorithmException {
        var commit = new Commit(treeHash, parentHash, author, author, message);
        return writeOject(commit);
    }
}












