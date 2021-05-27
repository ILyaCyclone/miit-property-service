package ru.unisuite.propertyclient;

import java.time.Duration;

public class PropertyServiceClientBuilder {
    private String propertyServiceUrl;
    private PropertyServiceHealthCheck healthCheck;

    private boolean cacheEnabled = true;
    private Long cacheCapacity;
    private Duration cacheExpirationDuration;


    /**
     * if not set, propertyServiceUrl will be resolved from env or system properties
     */
    public PropertyServiceClientBuilder propertyServiceUrl(String propertyServiceUrl) {
        this.propertyServiceUrl = propertyServiceUrl;
        return this;
    }

    public PropertyServiceClientBuilder noHealthCheck() {
        this.healthCheck = new NoopPropertyServiceHealthCheck();
        return this;
    }

    public PropertyServiceClientBuilder healthCheck(PropertyServiceHealthCheck propertyServiceHealthCheck) {
        this.healthCheck = propertyServiceHealthCheck;
        return this;
    }

    public PropertyServiceClientBuilder noCache() {
        this.cacheEnabled = false;
        return this;
    }

    public PropertyServiceClientBuilder cached() {
        this.cacheEnabled = true;
        return this;
    }

    public PropertyServiceClientBuilder cacheEnabled(boolean cacheEnabled) {
        this.cacheEnabled = cacheEnabled;
        return this;
    }


    public PropertyServiceClientBuilder cache(long cacheCapacity, Duration cacheExpirationDuration) {
        this.cacheEnabled = true;
        this.cacheCapacity = cacheCapacity;
        this.cacheExpirationDuration = cacheExpirationDuration;
        return this;
    }



    public PropertyServiceClient build() {
        String propertyServiceBaseUrl = this.propertyServiceUrl != null ? this.propertyServiceUrl : PropertyResolver.resolvePropertyServiceBaseUrl();

        if (propertyServiceBaseUrl == null || propertyServiceBaseUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("propertyServiceBaseUrl cannot be empty");
        }
        if (propertyServiceBaseUrl.endsWith("/")) {
            // remove trailing slash
            propertyServiceBaseUrl = propertyServiceBaseUrl.substring(0, propertyServiceBaseUrl.length() - 1);
        }

        PropertyServiceHealthCheck healthCheck = this.healthCheck != null ? this.healthCheck : new DefaultPropertyServiceHealthCheck();

        PropertyServiceFetch propertyServiceFetch = new PropertyServiceFetchImpl(propertyServiceBaseUrl);
        if (cacheEnabled) {
            if (cacheCapacity != null && cacheExpirationDuration != null) {
                propertyServiceFetch = new PropertyServiceFetchCachedCaffeine(propertyServiceFetch, cacheCapacity, cacheExpirationDuration);
            } else {
                propertyServiceFetch = new PropertyServiceFetchCachedCaffeine(propertyServiceFetch);
            }
        }

        PropertyServiceClient propertyServiceClient = new PropertyServiceClient(propertyServiceBaseUrl, healthCheck, propertyServiceFetch);
        propertyServiceClient.setCacheEnabled(cacheEnabled);
        return propertyServiceClient;
    }
}
