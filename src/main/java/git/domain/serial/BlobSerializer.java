package git.domain.serial;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import git.domain.Blob;

public class BlobSerializer implements ObjectContentSerializer<Blob> {

    @Override
    public void serialize(Blob blob, DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.write(blob.data());
    }

    @Override
    public Blob deserialize(DataInputStream dataInputStream) throws IOException {
        final var data = dataInputStream.readAllBytes();

        return new Blob(data);
    }

}