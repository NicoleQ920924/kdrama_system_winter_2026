package com.kdrama.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kdrama.backend.config.TmdbProperties;
import com.kdrama.backend.enums.DramaStatus;
import com.kdrama.backend.enums.KRReleaseSchedule;
import com.kdrama.backend.model.Drama;
import com.kdrama.backend.util.DramaFilterUtil;
import com.kdrama.backend.util.JsonNodeRequest;
import com.kdrama.backend.util.KRReleaseScheduleChecker;

import java.io.IOException;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.io.File;

import org.springframework.stereotype.Component;

@Component
public class TmdbDramaClient {

    private final TmdbProperties tmdbProperties;
    private final ObjectMapper objectMapper;

    public TmdbDramaClient(TmdbProperties tmdbProperties, ObjectMapper objectMapper) {
        this.tmdbProperties = tmdbProperties;
        this.objectMapper = objectMapper;
    }

    private static final String BASE_URL = "https://api.themoviedb.org/3/tv";

    private static final String BASE_SEARCH_URL = "https://api.themoviedb.org/3/search/tv";

    // Legacy method for searching for TMDB ID by Chinese name (only used in LLM mistakes correction)
    
    public Integer getDramaTmdbIdByDramaName(String chineseName) throws IOException {
        String tmdbApiKey = tmdbProperties.getKey();
        String query = URLEncoder.encode(chineseName, "UTF-8");

        String requestUrl = BASE_SEARCH_URL + "?api_key=" + tmdbApiKey + "&language=zh-TW&query=" + query;

        try {
            JsonNode results = JsonNodeRequest.getJsonNodebyRequestQuery(requestUrl);
            if (results != null && results.isArray() && results.size() > 0) {
                for (JsonNode node : results) {
                    String fetchedOriginalLanguage = node.path("original_language").asText();
                    if (fetchedOriginalLanguage.equals("ko")) {
                        return node.path("id").asInt();
                    }
                }
            }
            return null;

        } catch (Exception e) {
            System.out.println("Exception Occurred!" + e.getMessage());
            e.printStackTrace();
            return null;
        }      
    }

    public Drama fillDramaOtherInfo(Drama drama) throws IOException {
        String tmdbApiKey = tmdbProperties.getKey();
        if (drama == null) {
            return null;
        }

        try {
            String requestUrl = BASE_URL + "/" + drama.getTmdbId() +"?api_key=" + tmdbApiKey + "&language=zh-TW";
            JsonNode results = JsonNodeRequest.getJsonNodebyRequestQuery(requestUrl);

            if (results != null && !results.path("status_message").asText().equals("The resource you requested could not be found.")) {

                // Genres
                List<String> genres = new ArrayList<String>();
                JsonNode genreNodes = results.path("genres");
                if (genreNodes.isArray()) {
                    for (JsonNode node : genreNodes) {
                        String genre = node.path("name").asText();
                        genres.add(genre);
                    }
                    drama.setGenres(genres);
                }

                // Network
                List<String> networkNames = new ArrayList<String>();
                JsonNode networkNodes = results.path("networks");
                if (networkNodes.isArray()) {
                    for (JsonNode node : networkNodes) {
                        if (node.path("origin_country").asText().equals("KR")) {
                            networkNames.add(node.path("name").asText());
                        }
                    }
                    drama.setNetworks(networkNames);
                }

                // Handling status
                JsonNode nextEpNode = results.path("next_episode_to_air");
                if (!nextEpNode.has("id")) {
                    drama.setStatus(DramaStatus.COMPLETED);
                }
                else if (nextEpNode.path("episode_number").asInt() == 1) {
                    drama.setStatus(DramaStatus.NOT_AIRED);
                }
                else {
                    drama.setStatus(DramaStatus.ONGOING);
                }
                
                // Save the information of the drama to a .json file
                String backupFilePath = "backup/drama_backup.json";
                File backupFile = new File(backupFilePath);
                backupFile.getParentFile().mkdirs();
                objectMapper.writerWithDefaultPrettyPrinter().writeValue(backupFile, drama);

                return drama;
            }
            return null;

        } catch (Exception e) {
            System.out.println("Exception Occurred!" + e.getMessage());
            e.printStackTrace();
            return null;
        }      
    }

    public Drama fillDramaSeasonalInfo(Drama drama) throws IOException {
        String tmdbApiKey = tmdbProperties.getKey();

        if (drama == null) {
            return null;
        }
        
        try {
            String requestUrl = BASE_URL + "/" + drama.getTmdbId() + "/season/" + drama.getSeasonNumber() +"?api_key=" + tmdbApiKey + "&language=zh-TW";
            JsonNode results = JsonNodeRequest.getJsonNodebyRequestQuery(requestUrl);
            
            if (results == null || results.isNull()) {
                return null;
            }

            if (!results.path("status_message").asText().equals("The resource you requested could not be found.")) {
                // Fill in information from the fetched object
                // A condition "original_country = 'KR'" added on 2025/8/16
                if (!results.path("air_date").isNull() && (results.path("networks").path(0).path("origin_country").asText().equals("KR"))) {
                    drama.setIntroPageUrl("https://www.themoviedb.org/tv/" + drama.getTmdbId());

                    // Total number of episodes of a season
                    int totalNumOfEps = 0;
                    drama.setTotalNumOfEps(results.path("episodes").size());
                    for (int i = 0; i < results.path("episodes").size(); i++) {
                        totalNumOfEps = results.path("episodes").path(i).path("episode_number").asInt();
                        if (results.path("episode_type").asText().equals("finale")) {
                            drama.setTotalNumOfEps(totalNumOfEps);
                            break;
                        }
                    }

                    // Main Poster Url
                    drama.setMainPosterUrl("https://image.tmdb.org/t/p/original" + results.path("poster_path").asText());

                    // Fetch the episodes
                    JsonNode episodes = results.path("episodes");

                    // Fetch each episode -> get runtime and air date
                    int minRuntime = 0;
                    int maxRuntime = 0;
                    int epRuntime = 0;
                    ArrayList<String> epReleaseDates = new ArrayList<String>();

                    for (JsonNode ep : episodes) {
                        // Release Schedule (2025/8/16: Sometimes there are null dates; need to ignore them)
                        if (!ep.path("air_date").isNull()) {
                            epReleaseDates.add(ep.path("air_date").asText());
                        }
                        
                        // Runtime
                        JsonNode epRuntimeNode = ep.path("runtime");
                        if (epRuntimeNode.isNull()) {
                            continue;
                        }
                        else {
                            epRuntime = epRuntimeNode.asInt();

                            if (ep.path("episode_number").asText().equals("1")) {
                                minRuntime = epRuntime;
                                maxRuntime = epRuntime;
                            }
                            else if (epRuntime > maxRuntime) {
                                maxRuntime = epRuntime;
                            }
                            else if (epRuntime < minRuntime) {
                                minRuntime = epRuntime;
                            }
                        }
                    }

                    // Store Runtime
                    String estRuntimePerEp = ""; 
                    if (minRuntime == maxRuntime) {
                        estRuntimePerEp += minRuntime;
                    }
                    else {
                        estRuntimePerEp = minRuntime + "-" + maxRuntime;
                    }
                    drama.setEstRuntimePerEp(estRuntimePerEp);

                    // Check Schedule: refer to KRReleaseScheduleChecker
                    KRReleaseSchedule krReleaseSchedule = KRReleaseScheduleChecker.checkSchedule(epReleaseDates);
                    drama.setKrReleaseSchedule(krReleaseSchedule);

                    // Handling releaseYear
                    String firstEpReleaseYear = episodes.get(0).path("air_date").asText().substring(0, 4);
                    String lastEpReleaseYear = episodes.get(episodes.size() - 1).path("air_date").asText().substring(0, 4);
                    if (firstEpReleaseYear.equals(lastEpReleaseYear)) {
                        drama.setReleaseYear(firstEpReleaseYear);
                    }
                    else if (lastEpReleaseYear == null || lastEpReleaseYear.isEmpty() || lastEpReleaseYear.equals("null")) {
                        drama.setReleaseYear(firstEpReleaseYear);
                    }
                    else {
                        drama.setReleaseYear(firstEpReleaseYear + "-" + lastEpReleaseYear);
                    }

                    // Handling currentEpNo
                    int airedEps = 0;
                    for (String dateStr : epReleaseDates) {
                        LocalDate date = LocalDate.parse(dateStr);
                        if (date.isBefore(LocalDate.now())) { // there could be an error of 1 day due to time difference
                            airedEps++;
                        }
                    }
                    drama.setCurrentEpNo(airedEps);

                    // Save the information of the drama to a .json file
                    String backupFilePath = "backup/drama_backup.json";
                    File backupFile = new File(backupFilePath);
                    backupFile.getParentFile().mkdirs();
                    objectMapper.writerWithDefaultPrettyPrinter().writeValue(backupFile, drama);

                    return drama;
                }
                else {
                    return null;
                }

            }
                
            return null;
                
        } catch (Exception e) {
            System.out.println("Exception Occurred!" + e.getMessage());
            e.printStackTrace();
            return null;
        }      
    }

    public Drama fillKrAgeRestriction(Drama drama) throws IOException {
        String tmdbApiKey = tmdbProperties.getKey();

        if (drama == null) {
            return null;
        }

        String requestUrl = BASE_URL + "/" + drama.getTmdbId() +"/content_ratings?api_key=" + tmdbApiKey;

        try {
            JsonNode results = JsonNodeRequest.getJsonNodebyRequestQuery(requestUrl);

            if (results == null || results.isNull()) {
                return null;
            }

            if (!results.path("status_message").asText().equals("The resource you requested could not be found.")) {

                // Fill in the Korean Age Restriction
                if (results.isArray()) {                 
                    for (JsonNode node : results) {
                        if (node.path("iso_3166_1").asText().equals("KR")) {
                            drama.setKrAgeRestriction(node.path("rating").asInt());
                        }
                    }
                }

                // Save the information of the drama to a .json file
                String backupFilePath = "backup/drama_backup.json";
                File backupFile = new File(backupFilePath);
                backupFile.getParentFile().mkdirs();
                objectMapper.writerWithDefaultPrettyPrinter().writeValue(backupFile, drama);

                return drama;
            }
            return null;

        } catch (Exception e) {
            System.out.println("Exception Occurred!" + e.getMessage());
            e.printStackTrace();
            return null;
        }      
    }

    public Drama fillDramaStaff (Drama drama, String searchedActorName) throws IOException {
        String tmdbApiKey = tmdbProperties.getKey();

        if (drama == null) {
            return null;
        }
      
        try {
            String requestUrl = BASE_URL + "/" + drama.getTmdbId() + "/season/" + drama.getSeasonNumber() + "/credits?api_key=" + tmdbApiKey + "&language=zh-TW";

            JsonNode results = JsonNodeRequest.getJsonNodebyRequestQuery(requestUrl);

            if (results == null || results.isNull()) {
                return null;
            }

            if (!results.path("status_message").asText().equals("The resource you requested could not be found.")) {
                // Fill in additional information from the fetched object
                // Director and Scriptwriter Names
                List<String> directorNames = new ArrayList<String>();
                List<String> scriptwriterNames = new ArrayList<String>();
                JsonNode crewNodes = results.path("crew");
                if (crewNodes.isArray()) {
                    for (JsonNode node : crewNodes) {
                        String job = node.path("job").asText();
                        if (job.equals("Director") || job.equals("Co-Director")) {
                            directorNames.add(node.path("name").asText());
                        }
                        else if (job.equals("Writer")) {
                            scriptwriterNames.add(node.path("name").asText());
                        }
                    }
                    drama.setDirectorNames(directorNames);
                    drama.setScriptwriterNames(scriptwriterNames);
                }

                // Lead Actors' Names
                List<String> castNames = new ArrayList<String>();
                JsonNode castNodes = results.path("cast");
                int castCount = 1; // Max: 10
                if (castNodes.isArray()) {
                    for (JsonNode node : castNodes) {
                        String name = node.path("name").asText();
                        castNames.add(name);
                        castCount++;
                        if (castCount > 10) {
                            break;
                        }
                    }
                    drama.setLeadActors(castNames);
                    if (searchedActorName != null) {
                        if (!castNames.contains(searchedActorName)) {
                            return null;
                        }
                    }
                }

                // Save the information of the drama to a .json file
                String backupFilePath = "backup/drama_backup.json";
                File backupFile = new File(backupFilePath);
                backupFile.getParentFile().mkdirs();
                objectMapper.writerWithDefaultPrettyPrinter().writeValue(backupFile, drama);

                return drama;
            }
            else {
                return null;
            }
        } catch (Exception e) {
            System.out.println("Exception Occurred!" + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public boolean isDramaFromTmdbByTmdbId (Integer tmdbId) throws IOException {
        String tmdbApiKey = tmdbProperties.getKey();
        String requestUrl = BASE_URL + "/" + tmdbId + "?api_key=" + tmdbApiKey + "&language=zh-TW";

        boolean isDrama = false;

        try {
            JsonNode results = JsonNodeRequest.getJsonNodebyRequestQuery(requestUrl);
            if (!results.path("status_message").asText().equals("The resource you requested could not be found.")) {
                // Check via "type" and "genres"
                String type = results.path("type").asText();
                List<String> genres = new ArrayList<String>();
                JsonNode genreNodes = results.path("genres");
                if (genreNodes.isArray()) {
                    for (JsonNode node : genreNodes) {
                        String genre = node.path("name").asText();
                        genres.add(genre);
                    }
                }

                if (type.equals("Reality") || type.equals("Talk Show") || type.equals("News") || genres.contains("真人秀")) {
                    // Variety
                    isDrama = false;
                } else if ((type.equals("Scripted") || type.equals("Miniseries")) && genres.size() > 0) {
                    // Drama
                   
                    if (DramaFilterUtil.isValidKDrama(results)) {
                        isDrama = true;
                    }
                    else {
                        isDrama = false; // Web Drama instead
                    }
                }
            }

            return isDrama;

        } catch (Exception e) {
            System.out.println("Exception Occurred!" + e.getMessage());
            e.printStackTrace();
            return isDrama;
        }      
    }
}

