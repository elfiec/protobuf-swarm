package com.gmail.spb.elf.protobuf.swarm.travel;

import com.gmail.spb.elf.protobuf.swarm.model.Field;
import com.gmail.spb.elf.protobuf.swarm.model.Message;
import com.gmail.spb.elf.protobuf.swarm.model.Type;

import java.util.Optional;

/**
 * @author Dmitrii Sukhikh
 */
public class SwarmTraveler {

    private final Message message;

    public SwarmTraveler(Type type) {
        if (type instanceof Message) {
            this.message = (Message) type;
        } else {
            this.message = null;
        }
    }

    public SwarmTraveler follow(String name) {

        if (message == null) {
            return null;
        }

        Field field = message.getField(name);
        if (field != null) {
            return new SwarmTraveler(field.getType());
        } else {
            return null;
        }
    }

    public Optional<Message> getMessage() {
        return Optional.ofNullable(message);
    }
}
