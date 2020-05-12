package ru.unisuite.propertyclient;

public enum OutputFormat {
    COMMA_SEPARATED(""), JSON("/json");

    private final String pathPart;

    OutputFormat(String pathPart) {
        this.pathPart = pathPart;
    }

    String getPathPart() {
        return this.pathPart;
    }
}
