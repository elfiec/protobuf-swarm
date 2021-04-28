package com.gmail.spb.elf.protobuf.swarm.model;

import com.google.common.base.MoreObjects;

/**
 * It isn't a part of public api.
 * Exists in the model only during parsing.
 * @author Dmitrii Sukhikh
 */
public class UnresolvedReference extends Type {

    public UnresolvedReference(String name) {
        super("", name);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper("Unresolved")
                .add("name", getName())
                .toString();
    }
}
