package git.entry;

import git.mode.TreeEntryMode;

import java.io.DataOutputStream;
import java.io.IOException;

public record TreeEntry(TreeEntryMode mode, String name, String hash)
        implements Comparable<TreeEntry> {

    public void serialize(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.write(mode.format().getBytes());
        dataOutputStream.write(' ');
        dataOutputStream.write(name.getBytes());
        dataOutputStream.write('\0');
        dataOutputStream.write(hash.getBytes());
    }

    @Override
    public int compareTo(TreeEntry other) {
        return name.compareTo(other.name);
    }
}