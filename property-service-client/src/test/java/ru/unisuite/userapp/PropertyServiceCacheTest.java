package ru.unisuite.userapp;

import io.javalin.Javalin;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.unisuite.propertyclient.PropertyServiceClient;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PropertyServiceCacheTest {

    private static Javalin server;
    private static int timesCalled = 0;

    @BeforeAll
    static void startMockServer() {
        server = Javalin.create().start();

        server.get("/property1", ctx -> {
            timesCalled++;
            ctx.result("property 1 value");
        });
        server.get("/property2", ctx -> {
            timesCalled++;
            ctx.result("property 2 value");
        });
    }

    @AfterAll
    static void stopMockServer() {
        server.stop();
    }

    @BeforeEach
    void resetTimesCalled() {
        timesCalled = 0;
    }

    @Test
    void cacheWorks() {
        int serverPort = server.port();
        PropertyServiceClient propertyServiceClient = PropertyServiceClient.builder("http://localhost:" + serverPort)
                .noHealthCheck()
                .build();

        assertEquals(0, timesCalled);

        propertyServiceClient.getProperty("property1");

        assertEquals(1, timesCalled);

        String property1CachedValue = propertyServiceClient.getProperty("property1");
        assertEquals("property 1 value", property1CachedValue, "cached property value mismatch");
        assertEquals(1, timesCalled, "cached value should not be queried from server");

        String property2value = propertyServiceClient.getProperty("property2");
        assertEquals("property 2 value", property2value, "property value mismatch");
        assertEquals(2, timesCalled, "not cached value should be queried from cache");
    }

    @Test
    void cacheInvalidation() {
        int serverPort = server.port();
        PropertyServiceClient propertyServiceClient = PropertyServiceClient.builder("http://localhost:" + serverPort)
                .noHealthCheck()
                .build();

        assertEquals(0, timesCalled);

        propertyServiceClient.getProperty("property1");

        assertEquals(1, timesCalled);

        propertyServiceClient.invalidateCache();

        String property1RequeriedValue = propertyServiceClient.getProperty("property1");
        assertEquals("property 1 value", property1RequeriedValue, "property value mismatch");
        assertEquals(2, timesCalled);
    }

    @Test
    void withoutCache() {
        int serverPort = server.port();
        PropertyServiceClient propertyServiceClient = PropertyServiceClient.builder("http://localhost:" + serverPort)
                .noHealthCheck()
                .noCache()
                .build();

        assertEquals(0, timesCalled);

        propertyServiceClient.getProperty("property1");

        assertEquals(1, timesCalled);

        propertyServiceClient.getProperty("property1");

        assertEquals(2, timesCalled);
    }

}