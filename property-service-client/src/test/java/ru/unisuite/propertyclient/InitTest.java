package ru.unisuite.propertyclient;

import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.ClearEnvironmentVariable;
import org.junitpioneer.jupiter.ClearSystemProperty;
import org.junitpioneer.jupiter.SetEnvironmentVariable;
import org.junitpioneer.jupiter.SetSystemProperty;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class InitTest {

    @Test
    @SetSystemProperty(key = PropertyResolver.BASE_URL_SYSTEM_PROPERTY_NAME, value = "http://zzz")
    @ClearEnvironmentVariable(key = PropertyResolver.BASE_URL_ENV_VARIABLE_NAME)
    void canBeCreatedWithSystemProperty() {
        PropertyServiceClient propertyServiceClient = new PropertyServiceClient(new NoopPropertyServiceHealthCheck());
        assertEquals("http://zzz", propertyServiceClient.getPropertyServiceUrl());
    }

    @Test
    @ClearSystemProperty(key = PropertyResolver.BASE_URL_SYSTEM_PROPERTY_NAME)
    @SetEnvironmentVariable(key = PropertyResolver.BASE_URL_ENV_VARIABLE_NAME, value = "http://zzz")
    void canBeCreatedWithEnvVariable() {
        PropertyServiceClient propertyServiceClient = new PropertyServiceClient(new NoopPropertyServiceHealthCheck());
        assertEquals("http://zzz", propertyServiceClient.getPropertyServiceUrl());
    }

    @Test
    @SetSystemProperty(key = PropertyResolver.BASE_URL_SYSTEM_PROPERTY_NAME, value = "http://zzz")
    @SetEnvironmentVariable(key = PropertyResolver.BASE_URL_ENV_VARIABLE_NAME, value = "http://aaa")
    void systemPropertyPriorityOverEnvVariable() {
        PropertyServiceClient propertyServiceClient = new PropertyServiceClient(new NoopPropertyServiceHealthCheck());
        assertEquals("http://zzz", propertyServiceClient.getPropertyServiceUrl());
    }

    @Test
    @ClearEnvironmentVariable(key = PropertyResolver.BASE_URL_ENV_VARIABLE_NAME)
    @ClearSystemProperty(key = PropertyResolver.BASE_URL_SYSTEM_PROPERTY_NAME)
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
