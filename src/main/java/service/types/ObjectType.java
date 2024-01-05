package service.types;

import service.objects.Blob;
import service.objects.Commit;
import service.objects.Object;
import service.objects.Tree;
import service.serializers.impl.BlobSerializer;
import service.serializers.ObjectSerializer;
import service.serializers.impl.CommitSerializer;
import service.serializers.impl.TreeSerializer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

public class ObjectType<T extends Object> {
    public static final ObjectType<Blob> BLOB = new ObjectType<>("blob", Blob.class, new BlobSerializer());
    public static final ObjectType<Tree> TREE = new ObjectType<>("tree", Tree.class, new TreeSerializer());
    public static final ObjectType<Commit> COMMIT = new ObjectType<>("commit", Commit.class, new CommitSerializer());

    public static final Collection<ObjectType<?>> TYPES = List.of(BLOB, TREE, COMMIT);

    private String name;
    private Class<T> clazz;
    private ObjectSerializer<T> serializer;

    public ObjectType() {
    }

    public ObjectType(String name, Class<T> clazz, ObjectSerializer<T> serializer) {
        this.name = name;
        this.clazz = clazz;
        this.serializer = serializer;
    }

    public String getName() {
        return name;
    }

    public Class<T> getObjectClass() {
        return clazz;
    }

    public ObjectSerializer<T> getSerializer() {
        return serializer;
    }

    public void serialize(T object, DataOutputStream dataOutputStream) throws IOException {
        serializer.serialize(object, dataOutputStream);
    }

    public T deserialize(DataInputStream dataInputStream) throws IOException {
        return serializer.deserialize(dataInputStream);
    }

    public static ObjectType byName(String name) {
        for (var type : TYPES) {
            if (type.name.equalsIgnoreCase(name)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown object type: " + name);
    }

    public static ObjectType byClass(Class<? extends Object> clazz) {
        for (var type : TYPES) {
            if (type.getObjectClass().equals(clazz)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown object class: " + clazz);
    }
}
