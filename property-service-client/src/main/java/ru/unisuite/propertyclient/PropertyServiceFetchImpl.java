package ru.unisuite.propertyclient;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ConnectException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

class PropertyServiceFetchImpl implements PropertyServiceFetch {
    private static final Logger logger = LoggerFactory.getLogger(PropertyServiceFetchImpl.class);

    private final String propertyServiceUrl;

    public PropertyServiceFetchImpl(String propertyServiceUrl) {
        this.propertyServiceUrl = propertyServiceUrl;
    }

    @Override
    public String fetchFromPropertyService(PropertyServiceFetchKey fetchKey) {
        OutputFormat outputFormat = fetchKey.getOutputFormat();
        PropertyType propertyType = fetchKey.getPropertyType();
        String[] propertyNames = fetchKey.getPropertyNames();

        String urlString = propertyServiceUrl + outputFormat.getPathPart() + propertyType.getPathPart()
                + "/" + String.join(",", propertyNames);
        logger.debug("requesting url: {}", urlString);

        try {
            //TODO configure timeouts or maybe take okHttpClient as parameter?
            String result = IOUtils.toString(new URL(urlString), StandardCharsets.UTF_8);

            logger.debug("result for url '{}' is: {}", urlString, result);
            return result;
        } catch (ConnectException e) {
            throw new PropertyServiceClientException("Could not connect to property service with url `" + urlString + '\'', e);
        } catch (Exception e) {
            throw new PropertyServiceClientException(e);
        }
    }

}
