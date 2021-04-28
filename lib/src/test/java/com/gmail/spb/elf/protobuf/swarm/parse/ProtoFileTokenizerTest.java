package com.gmail.spb.elf.protobuf.swarm.parse;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Dmitrii Sukhikh
 */
public class ProtoFileTokenizerTest {

    @Test
    public void windowsNewLineRecognizeTest() {
        ProtoFileTokenizer tokenizer = new ProtoFileTokenizer("option\r\noption");

        List<String> tokens = readAllTokensClean(tokenizer);

        Assert.assertEquals(Arrays.asList("option", "option"), tokens);
    }

    @Test
    public void unixNewLineRecognizeTest() {
        ProtoFileTokenizer tokenizer = new ProtoFileTokenizer("option\noption");

        List<String> tokens = readAllTokensClean(tokenizer);

        Assert.assertEquals(Arrays.asList("option", "option"), tokens);
    }

    @Test
    public void tokensTest() {
        ProtoFileTokenizer tokenizer = new ProtoFileTokenizer("package spb.elf;" +
                "message Base.Command {required int32 type=1;}");

        List<String> tokens = readAllTokensClean(tokenizer);

        Assert.assertEquals(Arrays.asList("package", "spb.elf", ";",
                "message", "Base.Command", "{",
                "required", "int32", "type", "=", "1", ";",
                "}"), tokens);
    }

    @Test
    public void cleanTokensTest() {
        ProtoFileTokenizer tokenizer = new ProtoFileTokenizer("package spb.elf; //comment");

        List<String> tokens = readAllTokensClean(tokenizer);

        Assert.assertEquals(Arrays.asList("package", "spb.elf", ";"), tokens);
    }

    private static List<String> readAllTokensClean(ProtoFileTokenizer tokenizer) {
        List<String> tokens = new ArrayList<>();
        String token;
        while (!(token = tokenizer.nextTokenClean()).equals("")) {
            tokens.add(token);
        }
        return tokens;
    }
}
