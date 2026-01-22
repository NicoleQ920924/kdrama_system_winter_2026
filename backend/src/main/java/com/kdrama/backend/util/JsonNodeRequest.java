package com.kdrama.backend.util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.util.Scanner;
import java.util.function.Supplier;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.resilience4j.ratelimiter.*;
import io.github.resilience4j.retry.*;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.decorators.Decorators;

public class JsonNodeRequest {
    private static final RateLimiter rateLimiter;
    private static final Retry retry;

    static {
        RateLimiterConfig rlConfig = RateLimiterConfig.custom()
                .limitRefreshPeriod(Duration.ofSeconds(1))
                .limitForPeriod(5)  // max 5 times per second
                .timeoutDuration(Duration.ofSeconds(2))
                .build();
        rateLimiter = RateLimiter.of("tmdbLimiter", rlConfig);

        RetryConfig retryConfig = RetryConfig.custom()
            .maxAttempts(3)
            .intervalFunction(IntervalFunction.ofExponentialBackoff(500, 2.0, 3000))
            .retryExceptions(IOException.class, RuntimeException.class)
            .build();
        retry = Retry.of("tmdbRetry", retryConfig);
    }

    public static JsonNode getJsonNodebyRequestQuery(String requestUrl) throws Exception {
        // With resilience
        try {
            Supplier<JsonNode> decorated = Decorators.ofSupplier(() -> fetchJson(requestUrl))
                .withRetry(retry)
                .withRateLimiter(rateLimiter)
                .decorate();
            return decorated.get();
        } catch (RequestNotPermitted e) {
            // 被 RateLimiter 擋下來（超過頻率限制）
            System.err.println("TMDB API rate limit exceeded: " + requestUrl);
            return null;

        } catch (io.github.resilience4j.retry.MaxRetriesExceededException e) {
            // 超過重試次數
            System.err.println("Max retries exceeded for: " + requestUrl);
            return null;

        } catch (Exception e) {
            // 其他意外錯誤
            System.err.println("Unexpected error while calling TMDB API: " + e.getMessage());
            return null;
        }
    }

    // Separate logic for fetching Json
    private static JsonNode fetchJson(String requestUrl) {
        try {
            URL url = new URL(requestUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
        
            try (Scanner sc = new Scanner(conn.getInputStream(), "UTF-8")) {
                StringBuilder jsonText = new StringBuilder();
                while (sc.hasNext()) {
                    jsonText.append(sc.nextLine());
                }

                ObjectMapper mapper = new ObjectMapper();
                JsonNode rootNode = mapper.readTree(ChineseCharacterConverter.toTraditional(jsonText.toString()));
                JsonNode resultsNode;
                if (rootNode.has("results")) {
                    resultsNode = rootNode.path("results");
                } else {
                    resultsNode = rootNode;
                }
                return resultsNode;
            }

        } catch (Exception e) {
            System.out.println("Exception Occurred while calling TMDB API: " + e.getMessage());
            return null;
        }
    }
}
