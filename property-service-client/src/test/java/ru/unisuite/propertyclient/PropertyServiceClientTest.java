package ru.unisuite.propertyclient;

import io.javalin.Javalin;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PropertyServiceClientTest {

    static PropertyServiceClient propertyServiceClient;

    @BeforeAll
    static void mockServer() {
        Map<String, String> serverResponses = new HashMap<>();
        serverResponses.put("/property1", "property1 value");
        serverResponses.put("/property1,property2", "property1 value,property2 value");
        serverResponses.put("/property1,property3.comma", "property1 value,property3\\,with\\,comma\\,value");
        serverResponses.put("/property1,nosuchproperty,property3.comma", "property1 value,,property3\\,with\\,comma\\,value");
        serverResponses.put("/env/5000", "org name");
        serverResponses.put("/env/5000,5001", "org name,123\\,456");

        Javalin server = Javalin.create().start();
        int serverPort = server.port();
        serverResponses.forEach((request, response) -> server.get(request, ctx -> ctx.result(response)));

        propertyServiceClient = new PropertyServiceClient("http://localhost:" + serverPort);
    }

    @Test
    void getProperty() {
        String value = propertyServiceClient.getProperty("property1");
        assertEquals("property1 value", value);
    }

    @Test
    void getMultipleProperties() {
        Map<String, String> result = propertyServiceClient.getProperties("property1", "property2");

        assertAll(
                () -> assertEquals("property1 value", result.get("property1"))
                , () -> assertEquals("property2 value", result.get("property2"))
        );
    }

    @Test
    void getPropertiesWithComma() {
        Map<String, String> result = propertyServiceClient.getProperties("property1", "property3.comma");

        assertAll(
                () -> assertEquals("property1 value", result.get("property1"))
                , () -> assertEquals("property3,with,comma,value", result.get("property3.comma"))
        );
    }

    @Test
    void getPropertiesWithCommaAndEmpty() {
        Map<String, String> result = propertyServiceClient.getProperties("property1", "nosuchproperty", "property3.comma");

        assertAll(
                () -> assertEquals("property1 value", result.get("property1"))
                , () -> assertNull(result.get("nosuchproperty"))
                , () -> assertEquals("property3,with,comma,value", result.get("property3.comma"))
        );
    }

    @Test
    void getEnvProperty() {
        assertEquals("org name", propertyServiceClient.getEnvProperty("5000"));
    }

    @Test
    void getEnvProperties() {
        Map<String, String> result = propertyServiceClient.getEnvProperties("5000", "5001");

        assertAll(
                () -> assertEquals("org name", result.get("5000"))
                , () -> assertEquals("123,456", result.get("5001"))
        );
    }
}