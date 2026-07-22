package com.liveverse.realtime.exception;

public class CoreServiceException extends RuntimeException{

    public CoreServiceException() {
    }

    public CoreServiceException(String message) {
        super(message);
    }

    public CoreServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public CoreServiceException(Throwable cause) {
        super(cause);
    }
}
