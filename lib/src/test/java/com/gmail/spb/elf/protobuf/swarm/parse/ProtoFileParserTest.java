package com.gmail.spb.elf.protobuf.swarm.parse;

import com.gmail.spb.elf.protobuf.swarm.model.Message;
import com.gmail.spb.elf.protobuf.swarm.model.ProtoFile;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.apache.commons.io.IOUtils.resourceToString;

/**
 * @author Dmitrii Sukhikh
 */
public class ProtoFileParserTest {

    @Test
    public void packageTest() {
        ProtoFile protoFile = ProtoFileParser.parse("package main;");

        Assert.assertEquals("Package parse", "main", protoFile.getPackage());
    }

    @Test
    public void simpleMessageTest() throws IOException {
        String proto = resourceToString("simple.proto", StandardCharsets.UTF_8, getClass().getClassLoader());
        ProtoFile protoFile = ProtoFileParser.parse(proto);

        protoFile.getMessage("main.Shape");
    }

    @Test
    public void messageWithFieldsTest() throws IOException {
        String proto = resourceToString("fields.proto", StandardCharsets.UTF_8, getClass().getClassLoader());
        ProtoFile protoFile = ProtoFileParser.parse(proto);

        Message shape = protoFile.getMessage("main.Shape");
        shape.getField("x");
        shape.getField("inches");
    }

    @Test
    public void extensionTest() throws IOException {
        String proto = resourceToString("extension.proto", StandardCharsets.UTF_8, getClass().getClassLoader());
        ProtoFile protoFile = ProtoFileParser.parse(proto);

        protoFile.getMessage("main.Square");
    }
}
