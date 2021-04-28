package com.gmail.spb.elf.protobuf.swarm.parse;

import com.gmail.spb.elf.protobuf.swarm.model.Type;

/**
 * @author Dmitrii Sukhikh
 */
@FunctionalInterface
public interface TypeResolver {

    Type resolve(String name, String namespace);
}
