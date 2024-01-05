package service.objects;

import service.author.AuthorSignature;
import service.types.ObjectType;
import service.objects.Object;

public record Commit(String treeHash, String parentHash, AuthorSignature author, AuthorSignature committer, String message) implements Object {}
