package com.gmail.spb.elf.protobuf.swarm.model;

import com.google.common.base.MoreObjects;

/**
 * Enum definition. At this time only enum's name is supported.
 * @author Dmitrii Sukhikh
 */
public class Enum extends Type {

    public Enum(String path, String name) {
        super(path, name);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper("Enum")
                .add("fullName", getFullName())
                .toString();
    }
}
