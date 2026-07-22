package com.liveverse.realtime.exception;

public class TargetSessionNotFoundException extends RuntimeException{

    public TargetSessionNotFoundException() {
    }

    public TargetSessionNotFoundException(String message) {
        super(message);
    }

    public TargetSessionNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public TargetSessionNotFoundException(Throwable cause) {
        super(cause);
    }
}
