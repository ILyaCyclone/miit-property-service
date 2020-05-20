package ru.unisuite.propertyclient;

import io.javalin.Javalin;
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
            // this is expected to fail because of service health check, but we actually check that system property is read
            PropertyServiceClientException exception = assertThrows(PropertyServiceClientException.class, PropertyServiceClient::new);
            assertEquals("Could not check property service health on url 'http://zzz'", exception.getMessage());
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

    @Test
    void performsHealthCheck() {
        Javalin server = Javalin.create().start();
        int serverPort = server.port();
        server.get("/health", ctx -> ctx.result("{\"application\": \"Tetris\"}"));

        try {
            PropertyServiceClientException exception = assertThrows(PropertyServiceClientException.class
                    , () -> new PropertyServiceClient("http://localhost:" + serverPort));

            assertEquals("Property service health check failed: application `property-service` expected, but met `Tetris'."
                    , exception.getMessage());
        } finally {
            server.stop();
        }

    }

}
