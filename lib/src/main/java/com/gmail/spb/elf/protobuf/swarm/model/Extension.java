package com.gmail.spb.elf.protobuf.swarm.model;

/**
 * It isn't a part of public api.
 * Exists in the model only during parsing. Extension becomes a field
 * of a base message.
 * @author Dmitrii Sukhikh
 */
public class Extension {
    private final String namespace;
    private final String base;
    private final Field field;

    public Extension(String namespace, String base, Field field) {
        this.namespace = namespace;
        this.base = base;
        this.field = field;
    }

    public String getBase() {
        return base;
    }

    public String getNamespace() {
        return namespace;
    }

    public Field getField() {
        return field;
    }
}
