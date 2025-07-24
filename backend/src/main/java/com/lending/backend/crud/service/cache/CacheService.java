package com.lending.backend.crud.service.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * A centralized service to interact with the application's cache manager.
 * Provides a simplified and robust API for cache operations.
 */
@Service
public class CacheService {

    private static final Logger logger = LoggerFactory.getLogger(CacheService.class);
    private final CacheManager cacheManager;

    public CacheService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    /**
     * Stores a value in the specified cache.
     *
     * @param cacheName The name of the cache (e.g., "products").
     * @param key       The key for the cached item.
     * @param value     The value to store.
     * @param <T>       The type of the value.
     */
    public <T> void put(String cacheName, String key, T value) {
        try {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.put(key, value);
                logger.debug("Cached value with key '{}' in cache '{}'.", key, cacheName);
            } else {
                logger.warn("Cache '{}' not found. Could not store value for key '{}'.", cacheName, key);
            }
        } catch (Exception e) {
            logger.error("Error putting value into cache '{}' with key '{}'", cacheName, key, e);
        }
    }

    /**
     * Retrieves a value from the cache, wrapped in an Optional.
     *
     * @param cacheName The name of the cache.
     * @param key       The key of the item to retrieve.
     * @param type      The class of the expected type.
     * @param <T>       The type of the value.
     * @return An {@link Optional} containing the value if found, otherwise an empty
     *         Optional.
     */
    public <T> Optional<T> get(String cacheName, String key, Class<T> type) {
        try {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                Cache.ValueWrapper valueWrapper = cache.get(key);
                if (valueWrapper != null && valueWrapper.get() != null) {
                    logger.debug("Cache hit for key '{}' in cache '{}'.", key, cacheName);
                    return Optional.of(type.cast(valueWrapper.get()));
                }
            } else {
                logger.warn("Cache '{}' not found. Could not retrieve value for key '{}'.", cacheName, key);
            }
        } catch (Exception e) {
            logger.error("Error getting value from cache '{}' with key '{}'", cacheName, key, e);
        }
        logger.debug("Cache miss for key '{}' in cache '{}'.", key, cacheName);
        return Optional.empty();
    }

    /**
     * Removes a single entry from a cache.
     *
     * @param cacheName The name of the cache.
     * @param key       The key to evict.
     */
    public void evict(String cacheName, String key) {
        try {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.evict(key);
                logger.debug("Evicted key '{}' from cache '{}'.", key, cacheName);
            } else {
                logger.warn("Cache '{}' not found. Could not evict key '{}'.", cacheName, key);
            }
        } catch (Exception e) {
            logger.error("Error evicting key '{}' from cache '{}'", key, cacheName, e);
        }
    }

    /**
     * Clears all entries from a cache.
     *
     * @param cacheName The name of the cache to clear.
     */
    public void clear(String cacheName) {
        try {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
                logger.info("Cleared all entries from cache '{}'.", cacheName);
            } else {
                logger.warn("Cache '{}' not found. Could not clear.", cacheName);
            }
        } catch (Exception e) {
            logger.error("Error clearing cache '{}'", cacheName, e);
        }
    }
}