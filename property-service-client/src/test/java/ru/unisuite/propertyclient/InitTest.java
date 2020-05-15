package ru.unisuite.propertyclient;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class InitTest {

    @Test
    void canBeCreatedWithSystemProperty() {
        final String propertyName = "ru.unisuite.propertyclient.baseurl";
        final String beforePropertyValue = System.getProperty(propertyName);
        System.setProperty(propertyName, "http://zzz");
        try {
            PropertyServiceClient propertyServiceClient = new PropertyServiceClient();
            Assertions.assertEquals("http://zzz", propertyServiceClient.getPropertyServiceUrl());
        } finally {
            if (beforePropertyValue != null) {
                System.setProperty(propertyName, beforePropertyValue);
            } else {
                System.clearProperty(propertyName);
            }
        }
    }

    @Test
    @EnabledIf(value = "systemEnvironment.get('RU_UNISUITE_PROPERTYCLIENT_BASEURL') == null"
            , reason = "skip because propertyServiceBaseUrl is set by environment variable")
    void throwsExceptionIfCreatedWithoutSettingUrl() {
        Exception exception = assertThrows(IllegalArgumentException.class, PropertyServiceClient::new);
        assertEquals("propertyServiceBaseUrl cannot be empty", exception.getMessage());
    }

    @Test
    void throwsExceptionIfBaseUrlIncorrect() {
        Exception exception = assertThrows(PropertyServiceClientException.class, () -> new PropertyServiceClient("zzz"));
        assertEquals("propertyServiceBaseUrl is not set correctly: 'zzz'. Should be set to property service absolute url."
                , exception.getMessage());
    }

}
