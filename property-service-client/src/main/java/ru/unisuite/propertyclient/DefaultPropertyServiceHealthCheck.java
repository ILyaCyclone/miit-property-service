package ru.unisuite.propertyclient;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

class DefaultPropertyServiceHealthCheck implements PropertyServiceHealthCheck {
    private static final Logger logger = LoggerFactory.getLogger(DefaultPropertyServiceHealthCheck.class);

    private final boolean failIfCouldNotCheck;
    private final boolean failIfApplicationNameMismatch;
    private final boolean failIfApplicationStatusNotUp;
    private final boolean failIfDbStatusNotUp;

    public DefaultPropertyServiceHealthCheck() {
        this(false, false, false, false);
    }

    public DefaultPropertyServiceHealthCheck(boolean failIfCouldNotCheck, boolean failIfApplicationNameMismatch, boolean failIfApplicationStatusNotUp, boolean failIfDbStatusNotUp) {
        this.failIfCouldNotCheck = failIfCouldNotCheck;
        this.failIfApplicationNameMismatch = failIfApplicationNameMismatch;
        this.failIfApplicationStatusNotUp = failIfApplicationStatusNotUp;
        this.failIfDbStatusNotUp = failIfDbStatusNotUp;
    }

    @Override
    public void check(String propertyServiceBaseUrl) {
        try {
            String healthReply = IOUtils.toString(new URL(propertyServiceBaseUrl + "/health"), StandardCharsets.UTF_8);
            JSONObject healthJson = new JSONObject(healthReply);

            String applicationName = healthJson.getString("application");
            if (!applicationName.equals("property-service")) {
                String message = "Property service health check failed: " +
                        "application name `property-service` expected, but met `" + applicationName + "'.";
                throwExceptionOrLog(failIfApplicationNameMismatch, message);
                return;
            }

            String status = healthJson.getString("status");
            if (!status.equals("UP")) {
                String message = "Property service health check warning: status 'UP' expected, but met `" + status + "`.";
                throwExceptionOrLog(failIfApplicationStatusNotUp, message);
            }

            String dbStatus = healthJson.getJSONObject("components").getJSONObject("db").getString("status");
            if (!dbStatus.equals("UP")) {
                String message = "Property service health check warning: db status 'UP' expected, but met `" + dbStatus + "`.";
                throwExceptionOrLog(failIfDbStatusNotUp, message);
            }
        } catch (MalformedURLException e) {
            throw new PropertyServiceClientException("propertyServiceBaseUrl is not set correctly: '"
                    + propertyServiceBaseUrl + "'. Should be set to property service absolute url.");
        } catch (Exception e) {
            String message = "Could not check property service health on url '" + propertyServiceBaseUrl + '\'';
            throwExceptionOrLog(failIfCouldNotCheck, message, e);
        }
    }

    private void throwExceptionOrLog(boolean doFail, String message) {
        if (doFail) {
            throw new PropertyServiceClientException(message);
        } else {
            logger.warn(message);
        }
    }

    private void throwExceptionOrLog(boolean doFail, String message, Exception cause) {
        if (doFail) {
            throw new PropertyServiceClientException(message, cause);
        } else {
            logger.warn(message, cause);
        }
    }
}
