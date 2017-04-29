package io.limberest.util;

public class BooleanFormatException extends IllegalArgumentException {

    public BooleanFormatException () {
        super();
    }

    public BooleanFormatException (String s) {
        super (s);
    }

    public static BooleanFormatException forInputString(String s) {
        return new BooleanFormatException("For input string: \"" + s + "\"");
    }
}
