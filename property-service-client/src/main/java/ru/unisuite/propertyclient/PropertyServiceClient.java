package ru.unisuite.propertyclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

/**
 * main client class
 */
public class PropertyServiceClient {
    private static final Logger logger = LoggerFactory.getLogger(PropertyServiceClient.class);

    private static final Pattern CSV_SEPARATOR_PATTERN = Pattern.compile("(?<!\\\\),"); // separate by , but not by \,

    private final String propertyServiceUrl;
    private final PropertyServiceFetch propertyServiceFetch;
    private boolean cacheEnabled = false;


    public static PropertyServiceClient forPropertyServiceBaseUrl(String propertyServiceBaseUrl) {
        return new PropertyServiceClientBuilder().propertyServiceUrl(propertyServiceBaseUrl).build();
    }

    public static PropertyServiceClientBuilder builder() {
        return new PropertyServiceClientBuilder();
    }

    public static PropertyServiceClientBuilder builder(String propertyServiceBaseUrl) {
        return new PropertyServiceClientBuilder().propertyServiceUrl(propertyServiceBaseUrl);
    }

    public static PropertyServiceClient defaultInstance() {
        return new PropertyServiceClientBuilder().build();
    }

    PropertyServiceClient(String propertyServiceBaseUrl, PropertyServiceHealthCheck healthCheck
            , PropertyServiceFetch propertyServiceFetch) {

        healthCheck.check(propertyServiceBaseUrl);

        this.propertyServiceUrl = propertyServiceBaseUrl;
        this.propertyServiceFetch = propertyServiceFetch;

        logger.info("Property service client is created with base url: {}", propertyServiceBaseUrl);
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
        String[] values = CSV_SEPARATOR_PATTERN.split(output);
        return Collections.unmodifiableMap(IntStream.range(0, values.length)
                .collect(HashMap::new, (map, i) -> map.put(propertyNames[i]
                        , values[i].length() == 0 ? null : unescapeCsv(values[i]))
                        , HashMap::putAll));
    }

    public boolean isCacheEnabled() {
        return cacheEnabled;
    }

    public void invalidateCache() {
        if (isCacheEnabled()) {
            ((PropertyServiceFetchCached) propertyServiceFetch).invalidateCache();
        } else {
            logger.warn("PropertyServiceClient::invalidateCache has no effect as cache is not applied");
        }
    }

    // info

    public String getPropertyServiceUrl() {
        return this.propertyServiceUrl;
    }



    void setCacheEnabled(boolean cacheEnabled) {
        this.cacheEnabled = cacheEnabled;
    }

    private String fetchFromPropertyService(PropertyType propertyType, OutputFormat outputFormat, String... propertyNames) {
        PropertyServiceFetchKey fetchKey = new PropertyServiceFetchKey(propertyType, outputFormat, propertyNames);
        return propertyServiceFetch.fetchFromPropertyService(fetchKey);
    }

    private String unescapeCsv(String value) {
        return value.replace("\\,", ","); // replace \, with ,
    }

}
