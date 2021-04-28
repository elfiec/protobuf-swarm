package com.gmail.spb.elf.protobuf.swarm.parse;

import com.gmail.spb.elf.protobuf.swarm.model.*;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Dmitrii Sukhikh
 */
public class ProtobufModelParser {

    private static final Logger LOG = LoggerFactory.getLogger(ProtobufModelParser.class);

    public ProtobufJavaModel parseAll(Path root) throws IOException {
        Preconditions.checkState(Files.isDirectory(root), "%s is not a directory", root);
        try(Stream<Path> protoFiles = Files.walk(root).filter(candidate -> candidate.getFileName().toString().endsWith(".proto"))) {
            Map<String, ProtoFile> parsedProtoFiles = protoFiles.collect(Collectors.toMap(
                    protoFile -> root.relativize(protoFile).toString().replace('\\', '/'),
                    ProtoFileParser::parse
            ));

            if (LOG.isInfoEnabled()) {
                parsedProtoFiles.forEach((name, file) -> LOG.info("Proto file {} -> {}", name, file));
            }

            parsedProtoFiles.values()
                    .forEach(protoFile -> {
                        protoFile.getTypes()
                                .forEach(type -> type.resolveReferences((name, namespace) -> resolve(name, namespace, protoFile, parsedProtoFiles)));
                        protoFile.getExtensions().forEach(extension -> {
                            Message baseMessage = (Message) resolve(extension.getBase(), extension.getNamespace(), protoFile, parsedProtoFiles);
                            Field extensionField = extension.getField().withType(resolve(
                                    extension.getField().getType().getName(),
                                    extension.getNamespace(),
                                    protoFile,
                                    parsedProtoFiles
                            ));
                            baseMessage.addField(extensionField);
                        });
                    });

            return new ProtobufJavaModel(parsedProtoFiles.values().stream());
        }
    }

    private Type resolve(String name, String namespace, ProtoFile protoFile, Map<String, ProtoFile> protoFiles) {

        List<String> namespaceParts = Arrays.stream(namespace.split("\\.")).collect(Collectors.toList());
        int count = namespaceParts.size();
        for (int i = 0; i <= count; i++) {
            String candidateFullName = String.join(".", namespaceParts) + (namespaceParts.size() > 0 ? "." : "") + name;
            Type candidate = resolve(candidateFullName, protoFile, protoFiles);
            if (candidate != null) {
                return candidate;
            } else {
                namespaceParts.remove(namespaceParts.size() - 1);
            }
        }
        throw new RuntimeException();
    }

    private Type resolve(String name, ProtoFile protoFile, Map<String, ProtoFile> protoFiles) {
        return Stream.concat(
                Stream.of(protoFile),
                protoFile.getImports().map(importFile -> checkNotNull(protoFiles.get(importFile), "There is no proto file '%s' to import", importFile))
        ).map(candidateFile -> candidateFile.getType(name))
                .filter(Objects::nonNull)
                .findFirst().orElse(null);
    }

    public static void main(String[] args) throws IOException {
        ProtobufJavaModel protobufModel = new ProtobufModelParser().parseAll(Paths.get("/home/dsuhih/projects/sheltershared/proto"));
        Message message = protobufModel.getMessage("com.my.games.shelter.protobuf.requests.BaseRequestProto", "Sh.ServerInteraction.BaseRequest");
        System.out.println(message);
    }
}
