package ru.unisuite.propertyclient;

public enum PropertyType {
    STANDARD(""), ENV("/env");

    private final String pathPart;

    PropertyType(String pathPart) {
        this.pathPart = pathPart;
    }

    String getPathPart() {
        return this.pathPart;
    }
}
