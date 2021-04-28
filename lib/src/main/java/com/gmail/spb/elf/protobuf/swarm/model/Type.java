package com.gmail.spb.elf.protobuf.swarm.model;

import com.gmail.spb.elf.protobuf.swarm.parse.TypeResolver;
import com.google.common.base.MoreObjects;

/**
 * This is a base class of all types.
 * @author Dmitrii Sukhikh
 */
public abstract class Type {
    private final String pack;
    private final String name;

    protected Type(String pack, String name) {
        this.pack = pack;
        this.name = name;
    }

    public String getPack() {
        return pack;
    }

    public String getName() {
        return name;
    }

    public String getFullName() {
        return pack + "." + name;
    }

    public void resolveReferences(TypeResolver resolver) {

    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper("Type")
                .add("fullName", getFullName())
                .toString();
    }
}
