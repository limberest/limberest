package io.limberest.service;

public class ResourcePath {

    private String path;
    
    public ResourcePath(String path) {
        this.path = path;
        if (path.equals("/"))
            segments = new String[0];
        else if (path.startsWith("/"))
            segments = path.substring(1).split("/");
        else
            segments = path.split("/");
    }
    
    private String[] segments;
    public String[] getSegments() { return segments; }
    
    public String getSegment(int i) {
        if (segments.length <= i)
            return null;
        return segments[i];
    }

    public String toString() {
        return path;
    }
    
    /**
     * True if submitted path agrees with this path (allowing for parameter segments).
     */
    public boolean isMatch(ResourcePath path) {
        if (path.segments.length < segments.length)
            return false;
        for (int i = 0; i < segments.length; i++) {
            String segment = segments[i];
            String pathSegment = path.segments[i];
            if (!pathSegment.equals(segment) && !isParameter(segment))
                return false;
        }
        return true;
    }
    
    
    public static boolean isParameter(String segment) {
        return segment.startsWith("{") && segment.endsWith("}");
    }
}
