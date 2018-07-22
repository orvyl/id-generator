package com.orvyl.generator.id;

public interface IDGenerator<R> {
    R getNextID() throws IDGenerationException;
}
