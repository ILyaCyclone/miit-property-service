package ru.unisuite.propertyclient;

import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.ClearEnvironmentVariable;
import org.junitpioneer.jupiter.ClearSystemProperty;
import org.junitpioneer.jupiter.SetEnvironmentVariable;
import org.junitpioneer.jupiter.SetSystemProperty;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PropertyResolverTest {

    @Test
    @SetSystemProperty(key = PropertyResolver.BASE_URL_SYSTEM_PROPERTY_NAME, value = "http://zzz")
    @ClearEnvironmentVariable(key = PropertyResolver.BASE_URL_ENV_VARIABLE_NAME)
    void resolveSystemProperty() {
        assertEquals("http://zzz", PropertyResolver.resolvePropertyServiceBaseUrl());
    }

    @Test
    @ClearSystemProperty(key = PropertyResolver.BASE_URL_SYSTEM_PROPERTY_NAME)
    @SetEnvironmentVariable(key = PropertyResolver.BASE_URL_ENV_VARIABLE_NAME, value = "http://zzz")
    void resolveEnvVariable() {
        assertEquals("http://zzz", PropertyResolver.resolvePropertyServiceBaseUrl());
    }

    @Test
    @SetSystemProperty(key = PropertyResolver.BASE_URL_SYSTEM_PROPERTY_NAME, value = "http://zzz")
    @SetEnvironmentVariable(key = PropertyResolver.BASE_URL_ENV_VARIABLE_NAME, value = "http://aaa")
    void systemPropertyPriorityOverEnvVariable() {
        PropertyServiceClient propertyServiceClient = new PropertyServiceClient(new NoopPropertyServiceHealthCheck());
        assertEquals("http://zzz", propertyServiceClient.getPropertyServiceUrl());
    }

}