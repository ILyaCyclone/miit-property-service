package ru.unisuite.propertyclient;

interface PropertyServiceFetchCached {

    void invalidateCache();

    void invalidateCache(PropertyServiceFetchKey fetchKey);
}
