package com.kdrama.backend.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CacheFileUtil {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void saveCacheToFile(String filename, Map<String, String> cache) throws IOException {
        File file = new File("cache/" + filename + ".json");
        file.getParentFile().mkdirs(); // 確保目錄存在
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, cache);
    }

    public static Map<String, String> loadCacheFromFile(String filename) throws IOException {
        File file = new File("cache/" + filename + ".json");
        if (!file.exists()) return new HashMap<>();
        return objectMapper.readValue(file, Map.class);
    }
}
