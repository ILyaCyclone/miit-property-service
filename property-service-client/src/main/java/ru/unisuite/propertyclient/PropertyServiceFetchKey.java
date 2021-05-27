package ru.unisuite.propertyclient;

import java.util.Arrays;

class PropertyServiceFetchKey {
    private final PropertyType propertyType;
    private final OutputFormat outputFormat;
    private final String[] propertyNames;

    PropertyServiceFetchKey(PropertyType propertyType, OutputFormat outputFormat, String[] propertyNames) {
        this.propertyType = propertyType;
        this.outputFormat = outputFormat;
        this.propertyNames = propertyNames;
    }

    PropertyType getPropertyType() {
        return propertyType;
    }

    OutputFormat getOutputFormat() {
        return outputFormat;
    }

    String[] getPropertyNames() {
        return propertyNames;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PropertyServiceFetchKey fetchKey = (PropertyServiceFetchKey) o;

        if (propertyType != fetchKey.propertyType) return false;
        if (outputFormat != fetchKey.outputFormat) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(propertyNames, fetchKey.propertyNames);
    }

    @Override
    public int hashCode() {
        int result = propertyType != null ? propertyType.hashCode() : 0;
        result = 31 * result + (outputFormat != null ? outputFormat.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(propertyNames);
        return result;
    }
}
