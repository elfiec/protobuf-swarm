package com.gmail.spb.elf.protobuf.swarm.model;

import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Ready to use model.
 * 'Java' because it use <i>java_package</i> and <i>java_outer_classname<i/> options as
 * advice for searching definitions. There is can be same type name in the one set
 * of proto files and only java-class helps distinguish them.
 * @author Dmitrii Sukhikh
 */
public class ProtobufJavaModel {

    private final Map<TypeKey, Type> types;

    public ProtobufJavaModel(Stream<ProtoFile> files) {
        types = files.flatMap(file -> file.getTypes()
                .map(type -> Pair.of(new TypeKey(file.getJavaClass(), type.getFullName()), type)))
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
    }

    public Message getMessage(String javaClass, String name) {
        return (Message) types.get(new TypeKey(javaClass, name));
    }

    public Stream<Message> getAllMessages() {
        return types.values().stream()
                .filter(type -> type instanceof Message)
                .map(type -> (Message) type);
    }

    private static final class TypeKey {
        private final String javaClass;
        private final String name;

        public TypeKey(String javaClass, String name) {
            this.javaClass = javaClass;
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TypeKey typeKey = (TypeKey) o;

            if (!javaClass.equals(typeKey.javaClass)) return false;
            return name.equals(typeKey.name);
        }

        @Override
        public int hashCode() {
            int result = javaClass.hashCode();
            result = 31 * result + name.hashCode();
            return result;
        }
    }
}
