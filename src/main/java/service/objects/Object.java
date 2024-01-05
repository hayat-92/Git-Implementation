package service.objects;

public sealed interface Object permits Blob, Commit, Tree {
}
