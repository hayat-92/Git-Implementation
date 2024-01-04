package service.entry;

import service.mode.TreeEntryMode;

import java.io.DataOutputStream;
import java.io.IOException;

public record TreeEntry(TreeEntryMode mode, String name, byte[] hash) implements Comparable<TreeEntry> {

    public void serialize(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.write(mode.format().getBytes());
        dataOutputStream.write(' ');
        dataOutputStream.write(name.getBytes());
        dataOutputStream.write('\0');
        dataOutputStream.write(hash);

    }

    @Override
    public int compareTo(TreeEntry o) {
        return name.compareTo(o.name);
    }

}
