package git.domain;

import git.entry.TreeEntry;

import java.util.List;

public record Tree(List<TreeEntry> entries) implements GitObject {
}
