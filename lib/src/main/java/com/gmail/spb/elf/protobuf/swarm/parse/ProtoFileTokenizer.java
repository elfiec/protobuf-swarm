package com.gmail.spb.elf.protobuf.swarm.parse;

import com.google.common.base.Preconditions;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/**
 * This class splits <i>.proto</i> file onto tokens/lexemes.
 * It has been created from object like parser. So it
 * has some rudiment design.
 * @author Dmitrii Sukhikh
 */
class ProtoFileTokenizer {

    private static final String ONE_CHAR_TOKENS = "{}=;[]";
    private static final String STOP_TOKEN_CHARS = "\r\n ";
    private static final String STOP_VALUE_CHARACTERS = ";";

    private Reader reader;

    private boolean usePrevious;
    private char previous;

    private boolean usePreviousToken;
    private String previousToken;

    public ProtoFileTokenizer(String text) {
        this.reader = new StringReader(text);
    }

    public String nextToken() {

        if (usePreviousToken) {
            usePreviousToken = false;
            return previousToken;
        }

        StringBuilder sb = new StringBuilder();
        char c = nextClean();

        if (c == '/') {
            char nextC = next();
            if (nextC == '/') {
                previousToken = "//";
                return "//";
            }
            if (nextC == '*') {
                previousToken = "/*";
                return "/*";
            } else {
                back();
            }
        }

        if (ONE_CHAR_TOKENS.indexOf(c) >= 0) {
            previousToken = String.valueOf(c);
            return String.valueOf(c);
        }

        for (;;) {

            if (c == 0) {
                return sb.toString();
            }

            if (ONE_CHAR_TOKENS.indexOf(c) == -1 && STOP_TOKEN_CHARS.indexOf(c) == -1) {
                sb.append(c);
                c = next();
            } else {
                back();
                previousToken = sb.toString();
                return sb.toString();
            }
        }
    }

    public String nextTokenClean() {
        for (;;) {
            String nextToken = nextToken();
            if ("//".equals(nextToken)) {
                toNewLine();
            } else if ("/*".equals(nextToken)) {
                readUntilCommentBlockEnd();
            } else {
                return nextToken;
            }
        }
    }

    public String readUntilCommentBlockEnd() {
        StringBuilder sb = new StringBuilder();
        boolean halfEndToken = false;
        for (;;) {
            char c = next();
            if (c == 0) {
                return sb.toString();
            }

            if (halfEndToken && c == '/') {
                sb.setLength(sb.length() - 1);
                return sb.toString();
            }

            halfEndToken = c == '*';

            sb.append(c);
        }
    }

    public void skipUntilToken(String token) {

        for (;;) {
            String nextToken = nextToken();
            if (nextToken.equals(token)) {
                backToken();
                return;
            }
        }
    }

    public String toNewLine() {
        StringBuilder sb = new StringBuilder();
        char c = next();
        sb.append(c);
        while (c != '\r' && c != '\n' && c != 0) {
            c = next();
            sb.append(c);
        }

        return sb.toString();
    }

    public void backToken() {
        Preconditions.checkState(!usePreviousToken, "Only one step back is allowed");
        usePreviousToken = true;
    }

    private char next() {

        if (usePrevious) {
            usePrevious = false;
        } else {
            int c;
            try {
                c = reader.read();
            } catch (IOException e) {
                throw new IllegalStateException("Read error", e);
            }

            if (c <= 0) {
                previous = 0;
            } else {
                previous = (char) c;
            }

        }
        return previous;
    }

    private char nextClean() {
        for (;;) {
            char c = next();
            if (c == 0 || c > ' ') {
                return c;
            }
        }
    }

    private void back() {
        Preconditions.checkState(!usePrevious, "Only one step back is allowed");
        usePrevious = true;
    }

    public Object nextValue() {

        char c = nextClean();

        if (c == '"' || c == '\'') {
            return nextString(c);
        }

        StringBuilder sb = new StringBuilder();
        while (c >= ' ' && STOP_VALUE_CHARACTERS.indexOf(c) < 0) {
            sb.append(c);
            c = this.next();
        }
        back();

        String value = sb.toString().trim();
        if (value.isEmpty()) {
            throw new IllegalStateException("Missing value");
        }
        return stringToValue(value);
    }

    private Object stringToValue(String value) {

        if ("true".equalsIgnoreCase(value)) {
            return Boolean.TRUE;
        } else if ("false".equalsIgnoreCase(value)) {
            return Boolean.FALSE;
        }

        char first = value.charAt(0);
        if ('0' <= first && first <= '9' || first == '-') {
            if (value.indexOf('.') > -1 || value.indexOf('e') > -1 || value.indexOf('E') > -1) {
                Double d = Double.valueOf(value);
                if (!d.isInfinite() && !d.isNaN()) {
                    return d;
                }
            } else {
                Long l = new Long(value);
                if (value.equals(l.toString())) {
                    if (l == l.intValue()) {
                        return l.intValue();
                    } else {
                        return l;
                    }
                }
            }
        }

        return value;
    }

    private String nextString(char quote) {

        StringBuilder sb = new StringBuilder();
        boolean escape = false;
        for (;;) {
            char c = next();
            switch (c) {
                case 0:
                    throw new IllegalStateException("String is unterminated");
                case '\\':
                    escape = true;
                    break;
                default:
                    if (c == quote && !escape) {
                        return sb.toString();
                    }
                    sb.append(c);
                    escape = false;
            }
        }
    }
}
