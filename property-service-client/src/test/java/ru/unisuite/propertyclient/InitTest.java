package ru.unisuite.propertyclient;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

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
    void canNotBeCreatedWithoutSettingUrl() {
        Assertions.assertThrows(IllegalArgumentException.class, PropertyServiceClient::new, "propertyServiceBaseUrl cannot be empty");
    }

}
