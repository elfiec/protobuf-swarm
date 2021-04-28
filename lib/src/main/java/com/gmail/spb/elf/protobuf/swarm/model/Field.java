package com.gmail.spb.elf.protobuf.swarm.model;

/**
 * A field of a message. It may be an extension.
 * @author Dmitrii Sukhikh
 */
public final class Field {
    private final FieldRule rule;
    private final Type type;
    private final String name;
    private final int index;
    private final boolean extension;
    private final String comment;

    public Field(FieldRule rule, Type type, String name, int index, boolean extension, String comment) {
        this.rule = rule;
        this.type = type;
        if (extension) {
            String typeFullName = type instanceof UnresolvedReference ? "" : type.getFullName() + ".";
            this.name = typeFullName + name;
        } else {
            this.name = name;
        }
        this.index = index;
        this.extension = extension;
        this.comment = comment;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public boolean isExtension() {
        return extension;
    }

    public boolean isRequired() {
        return rule == FieldRule.REQUIRED;
    }

    public boolean isOptional() {
        return rule == FieldRule.OPTIONAL;
    }

    public boolean isRepeated() {
        return rule == FieldRule.REPEATED;
    }

    public Field withType(Type type) {
        return new Field(rule, type, name, index, extension, comment);
    }

    public int getIndex() {
        return index;
    }

    /**
     * Return a field comment. The field comment is located
     * on the same line with a field.
     * @return field comment
     */
    public String getComment() {
        return comment;
    }

    @Override
    public String toString() {
        return rule.name().toLowerCase() + " " + type + " " + name + " = " + index;
    }
}

