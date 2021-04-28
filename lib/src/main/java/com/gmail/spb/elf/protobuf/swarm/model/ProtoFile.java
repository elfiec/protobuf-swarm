package com.gmail.spb.elf.protobuf.swarm.model;

import com.google.common.base.MoreObjects;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This class represents parsed single <i>.proto</i> file.
 * Messages can contain {@link UnresolvedReference}. Messages don't contain extensions,
 * they are represented separately.
 * @author Dmitrii Sukhikh
 */
public final class ProtoFile {
    private final String pack;
    private final List<String> imports;
    private final Map<String, Type> types;
    private final String javaClass;
    private List<Extension> extensions;

    public ProtoFile(String pack, List<String> imports, List<Type> types, List<Extension> extensions, String javaClass) {
        this.pack = pack;
        this.imports = imports;
        this.types = types.stream().collect(Collectors.toMap(Type::getFullName, Function.identity()));
        this.javaClass = javaClass;
        this.extensions = extensions;
    }

    public String getPackage() {
        return pack;
    }

    public Stream<Type> getTypes() {
        return types.values().stream();
    }

    public Stream<Extension> getExtensions() {
        return extensions.stream();
    }

    public Type getType(String name) {
        return types.get(name);
    }

    public Stream<String> getImports() {
        return imports.stream();
    }

    public String getJavaClass() {
        return javaClass;
    }

    public Message getMessage(String name) {
        Type type = checkNotNull(types.get(name), "There is no type with name %s", name);
        return (Message) type;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper("ProtoFile")
                .add("package", pack)
                .add("imports count", imports.size())
                .add("types count", types.size())
                .add("javaClass", javaClass)
                .add("extensions count", extensions.size())
                .toString();
    }
}
