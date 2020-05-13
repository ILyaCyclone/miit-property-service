package ru.unisuite.propertyclient;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class InitTest {

    @Test
    void canBeCreatedWithSystemProperty() {
        System.setProperty("ru.unisuite.propertyclient.baseurl", "http://zzz");
        PropertyServiceClient propertyServiceClient = new PropertyServiceClient();
        Assertions.assertEquals("http://zzz", propertyServiceClient.getPropertyServiceUrl());
        System.setProperty("ru.unisuite.propertyclient.baseurl", "");
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
