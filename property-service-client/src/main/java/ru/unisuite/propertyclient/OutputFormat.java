package ru.unisuite.propertyclient;

public enum OutputFormat {
    CSV(""), JSON("/json");

    private final String pathPart;

    OutputFormat(String pathPart) {
        this.pathPart = pathPart;
    }

    String getPathPart() {
        return this.pathPart;
    }
}
