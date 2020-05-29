package ru.unisuite.propertyclient;

class PropertyResolver {
    static final String BASE_URL_SYSTEM_PROPERTY_NAME = "ru.unisuite.propertyclient.baseurl";
    static final String BASE_URL_ENV_VARIABLE_NAME = "RU_UNISUITE_PROPERTYCLIENT_BASEURL";

    // Spring prioritizes system properties over environment variables, 12factor - the opposite
    // let's do Spring style
    private static final boolean SYSTEM_PROPERTY_OVER_ENV_VARIABLE = true;

    private PropertyResolver() {
    }

    static String resolvePropertyServiceBaseUrl() {
        String systemProperty = System.getProperty(BASE_URL_SYSTEM_PROPERTY_NAME);
        String environmentVariable = System.getenv(BASE_URL_ENV_VARIABLE_NAME);

        String primaryPropertyValue = SYSTEM_PROPERTY_OVER_ENV_VARIABLE ? systemProperty : environmentVariable;
        String secondaryPropertyValue = !SYSTEM_PROPERTY_OVER_ENV_VARIABLE ? systemProperty : environmentVariable;

        return primaryPropertyValue != null ? primaryPropertyValue : secondaryPropertyValue;
    }

    // currently not used because junit pioneer extensions require key to be constant
//    private static String systemPropertyToEnvProperty(String key) {
//        return key.replace('.', '_')
//                .replace("-", "")
//                .toUpperCase();
//    }
}
