package com.orvyl.generator.id;

public class IDGenerationException extends Exception {
    public IDGenerationException(String message) {
        super(message);
    }

    public IDGenerationException(String message, Throwable cause) {
        super(message, cause);
    }

    public IDGenerationException(Throwable cause) {
        super(cause);
    }

    public IDGenerationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
