package service;

import service.objects.Object;
import service.types.ObjectType;

import java.io.*;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;
import java.nio.file.Files;

public class Git {

    private static final byte[] OBJECT_TYPE_BLOB = "blob".getBytes();
    private static final byte[] SPACE = " ".getBytes();
    private static final byte[] NULL = {0};
    private final File root;

    Git(File root) {
        this.root = root;
    }

    public File getDotGit() {
        return new File(root, ".git");
    }

    public File getObjectsDirectory() {
        return new File(getDotGit(), "objects");
    }

    public File getRefsDirectory() {
        return new File(getDotGit(), "refs");
    }

    public File getHeadFile() {
        return new File(getDotGit(), "HEAD");
    }

    public byte[] catFile(String hash) throws FileNotFoundException, IOException {
        var firstTwo = hash.substring(0, 2);
        var rest = hash.substring(2);

        var file = Paths.get(getObjectsDirectory().getPath(), firstTwo, rest).toFile();


        try (
                var inputStream = new FileInputStream(file);
                var inflaterInputStream = new InflaterInputStream(inputStream);
        ) {
            var builder = new StringBuilder();

            int value;
            while ((value = inflaterInputStream.read()) != -1 && value != ' ') {
                builder.append((char) value);
            }

            var type = builder.toString();

            builder = new StringBuilder();
            while ((value = inflaterInputStream.read()) != -1 && value != 0) {
                builder.append((char) value);
            }

            var objectLength = Integer.parseInt(builder.toString());
            return inflaterInputStream.readAllBytes();
        }
    }


    public static Git init(File file) throws IOException {
        var git = new Git(file);
        var dotGit = git.getDotGit();
        if (dotGit.exists()) {
            throw new IOException("Git repository already exists at " + file.getAbsolutePath());
        }

        git.getObjectsDirectory().mkdirs();
        git.getRefsDirectory().mkdirs();

        var head = git.getHeadFile();
        head.createNewFile();
        Files.write(head.toPath(), "ref: refs/heads/master\n".getBytes());

        return git;
    }


    public static Git open(File file) throws IOException {
        var git = new Git(file);
        var dotGit = git.getDotGit();
        if (!dotGit.exists()) {
            throw new IOException("No Git repository found at " + file.getAbsolutePath());
        }

        return git;
    }

    public String hashFile(File File) throws IOException, NoSuchAlgorithmException {
        try (
                var inputStream = new FileInputStream(File);
        ) {
            return hashFile(inputStream.readAllBytes());
        }
    }

    public String hashFile(byte[] bytes) throws IOException, NoSuchAlgorithmException {
        var lengthBytes = String.valueOf(bytes.length).getBytes();
        var message = MessageDigest.getInstance("SHA-1");
        message.update(OBJECT_TYPE_BLOB);
        message.update(SPACE);
        message.update(lengthBytes);
        message.update(NULL);
        message.update(bytes);


        var hashBytes = message.digest();
        var hash = HexFormat.of().formatHex(hashBytes);

        var firstTwo = hash.substring(0, 2);
        var rest = hash.substring(2);
        var firstTwoPath = Paths.get(getObjectsDirectory().getPath(), firstTwo).toFile();
        firstTwoPath.mkdirs();
        var restPath = Paths.get(firstTwoPath.getPath(), rest).toFile();
        try (
                var outputStream = Files.newOutputStream(restPath.toPath());
                var deflaterOutputStream = new DeflaterOutputStream(outputStream);
        ) {
            deflaterOutputStream.write(OBJECT_TYPE_BLOB);
            deflaterOutputStream.write(SPACE);
            deflaterOutputStream.write(lengthBytes);
            deflaterOutputStream.write(NULL);
            deflaterOutputStream.write(bytes);
        }
        return hash;

    }

    public String writeOject(service.objects.Object object) throws IOException, NoSuchAlgorithmException {
        var objectType = ObjectType.byClass(object.getClass());
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
            message.update(objectType.getName().getBytes());
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
            var firstTwoDirectory = new File(getObjectsDirectory(), firstTwo);
            firstTwoDirectory.mkdirs();
            var rest = hash.substring(2);
            var restFile = new File(firstTwoDirectory, rest);

            try (
                    var outputStream = Files.newOutputStream(restFile.toPath());
                    var deflaterOutputStream = new DeflaterOutputStream(outputStream);
                    var inputStream = Files.newInputStream(tempPath);
            ) {
                deflaterOutputStream.write(OBJECT_TYPE_BLOB);
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

    public service.objects.Object getObject(String hash) throws IOException, NoSuchAlgorithmException {
        var firstTwo = hash.substring(0, 2);
        var rest = hash.substring(2);

        var file = Paths.get(getObjectsDirectory().getPath(), firstTwo, rest).toFile();

        try (
                var inputStream = new FileInputStream(file);
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
}
