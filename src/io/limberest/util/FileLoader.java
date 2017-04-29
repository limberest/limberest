package io.limberest.util;

import java.io.FileNotFoundException;
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
                throw new FileNotFoundException(path);
            // TODO: better way?
            int bytesRead = 0;
            byte[] contents = new byte[0];
            byte[] buffer = new byte[1024];
            while ((bytesRead = is.read(buffer)) != -1) {
                byte[] newContents = new byte[contents.length + bytesRead];
                System.arraycopy(contents, 0, newContents, 0, contents.length);
                System.arraycopy(buffer, 0, newContents, contents.length, bytesRead);
                contents = newContents;
            }
            return contents;
        }
    }
}
