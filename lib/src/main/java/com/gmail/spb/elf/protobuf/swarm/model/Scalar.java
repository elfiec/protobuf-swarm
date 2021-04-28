package com.gmail.spb.elf.protobuf.swarm.model;

/**
 * This class represents a scalar type.
 * @author Dmitrii Sukhikh
 */
public class Scalar extends Type {
    
    public static final Scalar DOUBLE = new Scalar("double");
    public static final Scalar FLOAT = new Scalar("float");
    public static final Scalar INT32 = new Scalar("int32");
    public static final Scalar INT64 = new Scalar("int64");
    public static final Scalar UINT32 = new Scalar("uint32");
    public static final Scalar UINT64 = new Scalar("uint64");
    public static final Scalar SINT32 = new Scalar("sint32");
    public static final Scalar SINT64 = new Scalar("sint64");
    public static final Scalar FIXED32 = new Scalar("fixed32");
    public static final Scalar FIXED64 = new Scalar("fixed64");
    public static final Scalar SFIXED32 = new Scalar("sfixed32");
    public static final Scalar SFIXED64 = new Scalar("sfixed64");
    public static final Scalar BOOL = new Scalar("bool");
    public static final Scalar STRING = new Scalar("string");
    public static final Scalar BYTES = new Scalar("bytes");
    
    private Scalar(String name) {
        super("", name);
    }

    @Override
    public String getFullName() {
        return getName();
    }

    @Override
    public String toString() {
        return getName();
    }
}
