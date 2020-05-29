package ru.unisuite.propertyclient;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ConnectException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

public class PropertyServiceClient {
    private static final Logger logger = LoggerFactory.getLogger(PropertyServiceClient.class);

    private final String propertyServiceUrl;

    public PropertyServiceClient() {
        this(PropertyResolver.resolvePropertyServiceBaseUrl());
    }

    public PropertyServiceClient(String propertyServiceBaseUrl) {
        this(propertyServiceBaseUrl, new DefaultPropertyServiceHealthCheck());
    }

    public PropertyServiceClient(PropertyServiceHealthCheck propertyServiceHealthCheck) {
        this(PropertyResolver.resolvePropertyServiceBaseUrl(), propertyServiceHealthCheck);
    }

    public PropertyServiceClient(String propertyServiceBaseUrl, PropertyServiceHealthCheck propertyServiceHealthCheck) {
        if (propertyServiceBaseUrl == null || propertyServiceBaseUrl.trim().length() == 0) {
            throw new IllegalArgumentException("propertyServiceBaseUrl cannot be empty");
        }
        if (propertyServiceBaseUrl.endsWith("/")) {
            // remove trailing slash
            propertyServiceBaseUrl = propertyServiceBaseUrl.substring(0, propertyServiceBaseUrl.length() - 1);
        }

        propertyServiceHealthCheck.check(propertyServiceBaseUrl);

        this.propertyServiceUrl = propertyServiceBaseUrl;

        logger.debug("Property service client is created with propertyServiceBaseUrl: {}", propertyServiceBaseUrl);
    }


    public String getProperty(String propertyName) {
        return getProperty(PropertyType.STANDARD, propertyName);
    }

    public String getEnvProperty(String propertyName) {
        return getProperty(PropertyType.ENV, propertyName);
    }

    public String getProperty(PropertyType propertyType, String propertyName) {
        return fetchFromPropertyService(propertyType, OutputFormat.CSV, propertyName);
    }


    public Map<String, String> getProperties(String... propertyNames) {
        return getProperties(PropertyType.STANDARD, propertyNames);
    }

    public Map<String, String> getEnvProperties(String... propertyNames) {
        return getProperties(PropertyType.ENV, propertyNames);
    }


    public Map<String, String> getProperties(PropertyType propertyType, String... propertyNames) {
        String output = fetchFromPropertyService(propertyType, OutputFormat.CSV, propertyNames);
        String[] values = output.split("(?<!\\\\),"); // separate by , but not by \,
        return Collections.unmodifiableMap(IntStream.range(0, values.length)
                .collect(HashMap::new, (map, i) -> map.put(propertyNames[i]
                        , values[i].length() == 0 ? null : unescapeCsv(values[i]))
                        , HashMap::putAll));
    }

    public String getPropertyServiceUrl() {
        return this.propertyServiceUrl;
    }



    private String fetchFromPropertyService(PropertyType propertyType, OutputFormat outputFormat, String... propertyNames) {
        String urlString = propertyServiceUrl + outputFormat.getPathPart() + propertyType.getPathPart()
                + "/" + String.join(",", propertyNames);
        logger.debug("requesting url: {}", urlString);

        try {
            //TODO configure timeouts
            String result = IOUtils.toString(new URL(urlString), StandardCharsets.UTF_8);

            logger.debug("result for url '{}' is: {}", urlString, result);
            return result;
        } catch (ConnectException e) {
            throw new PropertyServiceClientException("Could not connect to property service with url `"+urlString+'\'', e);
        } catch (Exception e) {
            throw new PropertyServiceClientException(e);
        }
    }

    private String unescapeCsv(String value) {
        return value.replaceAll("\\\\,", ","); // replace \, with ,
    }
}
