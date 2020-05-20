package ru.unisuite.propertyclient;

class NoopPropertyServiceHealthCheck implements PropertyServiceHealthCheck {
    @Override
    public void check(String propertyServiceBaseUrl) {
        // no action needed here
    }
}
