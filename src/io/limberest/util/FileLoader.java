package io.limberest.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileLoader {

    private String path;

    public FileLoader(String path) {
        this.path = path;
    }

    public byte[] readFromClassLoader() throws IOException {
        return readFromClassLoader(getClass().getClassLoader());
    }

    public byte[] readFromClassLoader(ClassLoader classLoader) throws IOException {
        try (InputStream is = classLoader.getResourceAsStream(path.startsWith("/") ? path : "/" + path)) {
            if (is == null)
                return null;
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int bytesRead;
            byte[] data = new byte[1024];
            while ((bytesRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, bytesRead);
            }
            buffer.flush();
            return buffer.toByteArray();
        }
    }
}
