package service.objects;

import service.entry.TreeEntry;

import java.util.List;

public record Tree(List<TreeEntry> entries) implements Object  {
}
