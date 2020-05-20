package ru.unisuite.propertyclient;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

class DefaultPropertyServiceHealthCheck implements PropertyServiceHealthCheck {
    private static final Logger logger = LoggerFactory.getLogger(DefaultPropertyServiceHealthCheck.class);

    @Override
    public void check(String propertyServiceBaseUrl) {
        try {
            String healthReply = IOUtils.toString(new URL(propertyServiceBaseUrl + "/health"), StandardCharsets.UTF_8);
            JSONObject healthJson = new JSONObject(healthReply);

            String application = healthJson.getString("application");
            if (!application.equals("property-service")) {
                throw new PropertyServiceClientException("Property service health check failed: " +
                        "application `property-service` expected, but met `" + application + "'.");
            }

            String status = healthJson.getString("status");
            if (!status.equals("UP")) {
                logger.warn("Property service health check warning: status 'UP' expected, but met `{}`.", status);
            }

            String dbStatus = healthJson.getJSONObject("components").getJSONObject("db").getString("status");
            if (!dbStatus.equals("UP")) {
                logger.warn("Property service health check warning: db status 'UP' expected, but met `{}`.", dbStatus);
            }
        } catch (MalformedURLException e) {
            throw new PropertyServiceClientException("propertyServiceBaseUrl is not set correctly: '"
                    + propertyServiceBaseUrl + "'. Should be set to property service absolute url.");
        } catch (IOException e) {
            throw new PropertyServiceClientException("Could not check property service health on url '" + propertyServiceBaseUrl + '\'', e);
        }
    }
}
