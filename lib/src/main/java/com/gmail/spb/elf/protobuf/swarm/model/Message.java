package com.gmail.spb.elf.protobuf.swarm.model;

import com.gmail.spb.elf.protobuf.swarm.parse.TypeResolver;
import com.google.common.base.MoreObjects;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This class represents a message.
 * @author Dmitrii Sukhikh
 */
public final class Message extends Type {

    private Map<String, Field> fields;
    private String comment;

    public Message(String path, String name, List<Field> fields, String comment) {
        super(path, name);
        this.fields = fields.stream().collect(Collectors.toMap(Field::getName, Function.identity()));
        this.comment = comment;
    }

    public Field getFieldStrict(String name) {
        return checkNotNull(
                getField(name),
                "There is no field with name '%s' in %s",
                name, this
        );
    }

    public Field getField(String name) {
        return fields.get(name);
    }

    public void resolveReferences(TypeResolver resolver) {
        fields = fields.values().stream()
                .map(field -> {
                    Type type = field.getType();
                    if (type instanceof UnresolvedReference) {
                        String namespace = getFullName();
                        return field.withType(resolver.resolve(type.getName(), namespace));
                    } else {
                        return field;
                    }
                })
                .collect(Collectors.toMap(Field::getName, Function.identity()));
    }

    public void addField(Field field) {
        fields.put(field.getName(), field);
    }

    public Stream<Field> getFields() {
        return fields.values().stream();
    }

    public Stream<Field> getSortedByIndexFields() {
        return fields.values().stream()
                .sorted(Comparator.comparingInt(Field::getIndex));
    }

    public String getComment() {
        return comment;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper("Message")
                .add("fullName", getFullName())
                .toString();
    }
}
