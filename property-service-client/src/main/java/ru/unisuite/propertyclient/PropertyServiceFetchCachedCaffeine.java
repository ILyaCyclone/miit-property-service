package ru.unisuite.propertyclient;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

class PropertyServiceFetchCachedCaffeine implements PropertyServiceFetch, PropertyServiceFetchCached {

    private static final long DEFAULT_CACHE_CAPACITY = 1000;
    private static final Duration DEFAULT_CACHE_EXPIRATION_DURATION = Duration.of(15, ChronoUnit.MINUTES);

    private final LoadingCache<PropertyServiceFetchKey, String> cache;

    public PropertyServiceFetchCachedCaffeine(PropertyServiceFetch propertyServiceFetch) {
        this(propertyServiceFetch, DEFAULT_CACHE_CAPACITY, DEFAULT_CACHE_EXPIRATION_DURATION);
    }

    public PropertyServiceFetchCachedCaffeine(PropertyServiceFetch propertyServiceFetch
            , long cacheCapacity, Duration cacheExpirationDuration) {
        cache = Caffeine.newBuilder()
                .maximumSize(cacheCapacity)
                .expireAfterWrite(cacheExpirationDuration)
//                .refreshAfterWrite(1, TimeUnit.MINUTES)
                .build(propertyServiceFetch::fetchFromPropertyService);
    }

    @Override
    public String fetchFromPropertyService(PropertyServiceFetchKey fetchKey) {
        return cache.get(fetchKey);
    }

    @Override
    public void invalidateCache() {
        cache.invalidateAll();
    }

    @Override
    public void invalidateCache(PropertyServiceFetchKey fetchKey) {
        cache.invalidate(fetchKey);
    }
}
