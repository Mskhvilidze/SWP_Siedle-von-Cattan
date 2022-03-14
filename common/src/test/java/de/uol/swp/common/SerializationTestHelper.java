package de.uol.swp.common;

import java.io.*;

/**
 * Helper class to test if an object is serializable
 * <p>
 * https://stackoverflow.com/questions/3840356/how-to-test-in-java-that-a-class-implements-serializable-correctly-not-just-is
 */

@SuppressWarnings({"PMD.CommentRequired", "PMD.AvoidCatchingGenericException"})
public class SerializationTestHelper {

    public static <T extends Serializable> byte[] pickle(T obj)
            throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(obj);
        oos.close();
        return baos.toByteArray();
    }

    public static <T extends Serializable> T unpickle(byte[] byteInput, Class<T> cls)
            throws IOException, ClassNotFoundException {
        ByteArrayInputStream bais = new ByteArrayInputStream(byteInput);
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object obj = ois.readObject();
        return cls.cast(obj);
    }

    public static <T extends Serializable> boolean checkSerializableAndDeserializable(T obj, Class<T> cls) {
        try {
            byte[] bytes = pickle(obj);
            T obj2 = unpickle(bytes, cls);
            return obj.equals(obj2);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
