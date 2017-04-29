package io.limberest.jackson;

import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;

public class JacksonPrettyPrinter extends DefaultPrettyPrinter {
    public JacksonPrettyPrinter(int indent) {
        super();
        StringBuilder indentSpaces = new StringBuilder();
        while (indentSpaces.length() < indent)
            indentSpaces.append(" ");
        super._objectIndenter = new DefaultIndenter(indentSpaces.toString(), DefaultIndenter.SYS_LF);
        super._arrayIndenter = DefaultIndenter.SYSTEM_LINEFEED_INSTANCE;
    }
}
