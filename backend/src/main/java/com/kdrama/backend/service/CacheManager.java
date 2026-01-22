package com.kdrama.backend.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.kdrama.backend.util.CacheFileUtil;

@Component
public class CacheManager {
    private final Map<String, Map<String, String>> caches = new HashMap<>();

    public void saveCache(String title, Map<String, String> cache) {
        caches.put(title, cache);
        try {
            CacheFileUtil.saveCacheToFile(title, cache);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<String, String> loadCache(String title) {
        try {
            Map<String, String> cache = CacheFileUtil.loadCacheFromFile(title);
            caches.put(title, cache);
            return cache;
        } catch (IOException e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    public Map<String, String> getCache(String title) {
        return caches.getOrDefault(title, new HashMap<>());
    }
}

