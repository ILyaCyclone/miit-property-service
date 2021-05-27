package ru.unisuite.propertyclient;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import io.javalin.Javalin;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

class DefaultPropertyServiceHealthCheckTest {

    @Test
    void failsIfMalformedUrl() {
        PropertyServiceClientException exception = assertThrows(PropertyServiceClientException.class
                , () -> PropertyServiceClient.forPropertyServiceBaseUrl("zzz"));

        assertEquals("propertyServiceBaseUrl is not set correctly: 'zzz'. Should be set to property service absolute url."
                , exception.getMessage());
    }

    @Test
    void logsIfHealthCheckFailed() {
        Javalin server = Javalin.create().start();
        int serverPort = server.port();
        // server does not respond to /health request
//        server.get("/health", ctx -> ctx.result("{\"application\": \"Tetris\"}"));
//        server.get("/health", ctx -> ctx.result("unexpected response"));

        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(DefaultPropertyServiceHealthCheck.class);
        ListAppender<ILoggingEvent> loggingAppender = new ListAppender<>();
        loggingAppender.start();

        logger.addAppender(loggingAppender);
        try {
            PropertyServiceClient.forPropertyServiceBaseUrl("http://localhost:" + serverPort);
        } finally {
            server.stop();
        }

        assertTrue(
                loggingAppender.list.stream()
                        .anyMatch(loggingEvent -> loggingEvent.getLevel().equals(Level.WARN)
                                && loggingEvent.getFormattedMessage()
                                .equals("Could not check property service health on url 'http://localhost:" + serverPort + '\''))
        );
    }


    @Test
    void logsIfUnexpectedApplicationName() {
        Javalin server = Javalin.create().start();
        int serverPort = server.port();
        server.get("/health", ctx -> ctx.result("{\"application\": \"Tetris\"}"));

        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(DefaultPropertyServiceHealthCheck.class);
        ListAppender<ILoggingEvent> loggingAppender = new ListAppender<>();
        loggingAppender.start();

        logger.addAppender(loggingAppender);
        try {
            PropertyServiceClient.forPropertyServiceBaseUrl("http://localhost:" + serverPort);
        } finally {
            server.stop();
        }

        assertTrue(
                loggingAppender.list.stream()
                        .anyMatch(loggingEvent -> loggingEvent.getLevel().equals(Level.WARN)
                                && loggingEvent.getFormattedMessage()
                                .equals("Property service health check failed: application name `property-service` expected, but met `Tetris'."))
        );
    }

}