package ru.unisuite.propertyclient;

public class PropertyServiceClientException extends RuntimeException {
    public PropertyServiceClientException(String message) {
        super(message);
    }

    public PropertyServiceClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public PropertyServiceClientException(Throwable cause) {
        super(cause);
    }
}
