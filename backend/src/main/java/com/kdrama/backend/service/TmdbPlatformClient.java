package com.kdrama.backend.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.kdrama.backend.config.TmdbProperties;
import com.kdrama.backend.util.JsonNodeRequest;

@Component
public class TmdbPlatformClient { // Netflix, Disney+, Prime Video, Apple TV, Catchplay
    private final TmdbProperties tmdbProperties;

    public TmdbPlatformClient(TmdbProperties tmdbProperties) {
        this.tmdbProperties = tmdbProperties;
    }

    private static final String DRAMA_BASE_URL = "https://api.themoviedb.org/3/tv";
    private static final String MOVIE_BASE_URL = "https://api.themoviedb.org/3/movie";

    private static final String NETFLIX_URL = "https://www.netflix.com";
    private static final String DISNEY_PLUS_URL = "https://www.disneyplus.com";
    private static final String AMAZON_PRIME_VIDEO_URL = "https://www.primevideo.com";
    private static final String APPLE_TV_URL = "https://tv.apple.com";
    private static final String CATCH_PLAY_URL = "https://www.catchplay.com";
    private static final String HBO_MAX_URL = "https://www.hbomax.com/tw";

    public Map<String, String> getIntlPlatformInfoByWorkTmdbId(Integer tmdbId, Integer seasonNumber, String workType) throws IOException {
        String tmdbApiKey = tmdbProperties.getKey();
        String requestUrl = "";

        Map<String, String> platformMap = new HashMap<String, String>();

        if (workType.equals("drama") && seasonNumber >= 2) {
            requestUrl = DRAMA_BASE_URL + "/" + tmdbId + "/season/" + seasonNumber + "/watch/providers?api_key=" + tmdbApiKey + "&language=zh-TW";
        }
        else if (workType.equals("drama") && seasonNumber == 1) {
            requestUrl = DRAMA_BASE_URL + "/" + tmdbId + "/watch/providers?api_key=" + tmdbApiKey + "&language=zh-TW";
        }
        else if (workType.equals("movie")) {
            requestUrl = MOVIE_BASE_URL + "/" + tmdbId + "/watch/providers?api_key=" + tmdbApiKey + "&language=zh-TW";
        }

        try {
            JsonNode results = JsonNodeRequest.getJsonNodebyRequestQuery(requestUrl);
            if (results.size() > 0) {
                if (workType == "drama") {
                    JsonNode twPlatformNodes = results.path("TW").path("flatrate");
                    if (twPlatformNodes.isArray()) {
                        for (JsonNode node : twPlatformNodes) {
                            String platformName = node.path("provider_name").asText();
                            String platformUrl = "";
                            switch (platformName) {
                                case "Netflix":
                                    platformUrl = NETFLIX_URL;
                                    break;
                                case "Disney Plus":
                                    platformUrl = DISNEY_PLUS_URL;
                                    break;
                                case "Amazon Prime Video":
                                    platformUrl = AMAZON_PRIME_VIDEO_URL;
                                    break;
                                case "Apple TV":
                                    platformUrl = APPLE_TV_URL;
                                    break;
                                case "Catchplay":
                                    platformUrl = CATCH_PLAY_URL;
                                    break;
                                case "HBO Max":
                                    platformUrl = HBO_MAX_URL;
                                    break;
                                default:
                                    break;
                            }
                            if (platformUrl != "") {
                                platformMap.put(platformName, platformUrl);
                            }
                        }
                    }
                }
                else if (workType == "movie") {
                    JsonNode twPlatformNodes = results.path("TW").path("rent");
                    if (twPlatformNodes.isArray()) {
                        for (JsonNode node : twPlatformNodes) {
                            String platformName = node.path("provider_name").asText();
                            String platformUrl = "";
                            switch (platformName) {
                                case "Netflix":
                                    platformUrl = NETFLIX_URL;
                                    break;
                                case "Disney Plus":
                                    platformUrl = DISNEY_PLUS_URL;
                                    break;
                                case "Amazon Prime Video":
                                    platformUrl = AMAZON_PRIME_VIDEO_URL;
                                    break;
                                case "Apple TV":
                                    platformUrl = APPLE_TV_URL;
                                    break;
                                case "Catchplay":
                                    platformUrl = CATCH_PLAY_URL;
                                    break;
                                case "HBO Max":
                                    platformUrl = HBO_MAX_URL;
                                    break;
                                default:
                                    break;
                            }

                            if (platformUrl != "") {
                                platformMap.put(platformName, platformUrl);
                            }
                        }
                    }
                }
            }
            return platformMap;

        } catch (Exception e) {
            System.out.println("Exception Occurred!" + e.getMessage());
            return platformMap;
        }      
    }
}
