package com.gmail.spb.elf.protobuf.swarm.parse;

import com.gmail.spb.elf.protobuf.swarm.model.Enum;
import com.gmail.spb.elf.protobuf.swarm.model.*;
import com.google.common.base.Preconditions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author Dmitrii Sukhikh
 */
class ProtoFileParser {

    public static ProtoFile parse(Path path) {
        try {
            String proto = new String(Files.readAllBytes(path));
            return parse(proto);
        } catch (IOException e) {
            throw new RuntimeException("Error on read", e);
        }
    }

    public static ProtoFile parse(String proto) {

        ProtoFileTokenizer tokenizer = new ProtoFileTokenizer(proto);

        String pack = "";
        String javaPackage = "";
        String javaOuterClassname = "";
        List<String> imports = new ArrayList<>();
        List<Extension> extensions = new ArrayList<>();
        List<Type> types = new ArrayList<>();
        for (; ; ) {
            String token = tokenizer.nextTokenClean();

            switch (token) {
                case "package":
                    pack = tokenizer.nextTokenClean();
                    Preconditions.checkState(tokenizer.nextTokenClean().equals(";"));
                    break;
                case "import":
                    imports.add((String) tokenizer.nextValue());
                    Preconditions.checkState(tokenizer.nextTokenClean().equals(";"));
                    break;
                case "option":
                    String optionName = tokenizer.nextTokenClean();
                    tokenizer.nextTokenClean(); //=
                    if (optionName.equals("java_package")) {
                        javaPackage = (String) tokenizer.nextValue();
                    } else if (optionName.equals("java_outer_classname")) {
                        javaOuterClassname = (String) tokenizer.nextValue();
                    }
                    tokenizer.toNewLine();
                    break;
                case "syntax":
                    tokenizer.toNewLine();
                    break;
                case "message":
                    ParseMessageResult result = parseMessage(tokenizer, pack);
                    types.addAll(result.getTypes());
                    extensions.addAll(result.getExtenstions());
                    break;
                case "enum":
                    String name = tokenizer.nextTokenClean();
                    types.add(new Enum(pack, name));
                    tokenizer.skipUntilToken("}");
                    tokenizer.nextTokenClean(); //}
                    break;
                default:
                    return new ProtoFile(pack, imports, types, extensions, javaPackage + "." + javaOuterClassname);
            }
        }
    }

    private static ParseMessageResult parseMessage(ProtoFileTokenizer tokenizer, String path) {

        String comment = tokenizer.consumeComments();

        String messageName = tokenizer.nextTokenClean();
        Preconditions.checkState(isSymbol(messageName));

        List<Field> fields = new ArrayList<>();
        List<Extension> extensions = new ArrayList<>();
        List<Type> types = new ArrayList<>();
        boolean open = false;
        for (; ; ) {
            String token = tokenizer.nextTokenClean();
            switch (token) {
                case "{":
                    Preconditions.checkState(!open);
                    open = true;
                    break;
                case "}":
                    Preconditions.checkState(open);
                    types.add(new Message(path, messageName, fields, comment));
                    return new ParseMessageResult(types, extensions);
                case "optional":
                case "required":
                case "repeated":
                    Field field = parseFieldWithType(tokenizer, parseFieldRule(token), false);
                    fields.add(field);
                    break;
                case "message":
                    ParseMessageResult result = parseMessage(tokenizer, (path.isEmpty() ? "" : path + ".") + messageName);
                    types.addAll(result.getTypes());
                    extensions.addAll(result.getExtenstions());
                    break;
                case "oneof":
                    parseOneOf(tokenizer)
                            .forEach(fields::add);
                    break;
                case "enum":
                    String name = tokenizer.nextTokenClean();
                    types.add(new Enum(path + "." + messageName, name));
                    tokenizer.skipUntilToken("}");
                    tokenizer.nextTokenClean();
                    break;
                case "extensions":
                    tokenizer.toNewLine();
                    break;
                case "extend":
                    String base = tokenizer.nextTokenClean();
                    tokenizer.nextTokenClean(); //{
                    Field extensionField = parseFieldWithType(tokenizer, parseFieldRule(tokenizer.nextTokenClean()), true);
                    tokenizer.nextTokenClean(); //}
                    extensions.add(new Extension(path + "." + messageName, base, extensionField));
                    break;
            }
        }
    }

    private static FieldRule parseFieldRule(String rule) {
        switch (rule) {
            case "required":
                return FieldRule.REQUIRED;
            case "optional":
                return FieldRule.OPTIONAL;
            case "repeated":
                return FieldRule.REPEATED;
            default:
                throw new RuntimeException("Unknown field rule '" + rule + "'");
        }
    }

    private static Field parseFieldWithType(ProtoFileTokenizer tokener, FieldRule rule, boolean extension) {
        String comment = tokener.consumeComments();
        String type = tokener.nextTokenClean();
        return parseField(tokener, rule, type, extension, comment);
    }

    private static boolean isSymbol(String token) {
        return token.chars().allMatch(c -> ('0' <= c && c <= '9') || ('A' <= c && c <= 'Z') || ('a' <= c && c <= 'z') || c == '.' || c == '_');
    }

    private static Field parseField(ProtoFileTokenizer tokenizer, FieldRule rule, String type, boolean extension, String comment) {
        String filedName = tokenizer.nextTokenClean();
        Preconditions.checkState(isSymbol(filedName));
        Preconditions.checkState(tokenizer.nextTokenClean().equals("="));
        int index = Integer.parseInt(tokenizer.nextTokenClean());
        String token = tokenizer.nextTokenClean();
        if ("[".equals(token)) {
            tokenizer.skipUntilToken("]");
            tokenizer.nextTokenClean(); //]
            token = tokenizer.nextTokenClean();
        }
        Preconditions.checkState(token.equals(";"));
        return new Field(rule, parseType(type), filedName, index, extension, comment);
    }

    private static Stream<Field> parseOneOf(ProtoFileTokenizer tokener) {
        tokener.nextTokenClean(); //oneof name
        boolean open = false;
        List<Field> fields = new ArrayList<>();
        for (; ; ) {
            String token = tokener.nextTokenClean();

            switch (token) {
                case "{":
                    Preconditions.checkState(!open);
                    open = true;
                    break;
                case "}":
                    Preconditions.checkState(open);
                    return fields.stream();
                default:
                    String type = token;
                    String comment = tokener.consumeComments();
                    Field field = parseField(tokener, FieldRule.OPTIONAL, type, false, comment);
                    fields.add(field);
            }
        }
    }

    private static Type parseType(String type) {
        switch (type) {
            case "double":
                return Scalar.DOUBLE;
            case "float":
                return Scalar.FLOAT;
            case "int32":
                return Scalar.INT32;
            case "int64":
                return Scalar.INT64;
            case "uint32":
                return Scalar.UINT32;
            case "uint64":
                return Scalar.UINT64;
            case "sint32":
                return Scalar.SINT32;
            case "sint64":
                return Scalar.SINT64;
            case "fixed32":
                return Scalar.FIXED32;
            case "fixed64":
                return Scalar.FIXED64;
            case "sfixed32":
                return Scalar.SFIXED32;
            case "sfixed64":
                return Scalar.SFIXED64;
            case "bool":
                return Scalar.BOOL;
            case "string":
                return Scalar.STRING;
            case "bytes":
                return Scalar.BYTES;
            default:
                return new UnresolvedReference(type);
        }
    }

    private static class ParseMessageResult {
        private final List<Type> types;
        private final List<Extension> extenstions;

        public ParseMessageResult(List<Type> types, List<Extension> extenstions) {
            this.types = types;
            this.extenstions = extenstions;
        }

        public List<Type> getTypes() {
            return types;
        }

        public List<Extension> getExtenstions() {
            return extenstions;
        }
    }
}
