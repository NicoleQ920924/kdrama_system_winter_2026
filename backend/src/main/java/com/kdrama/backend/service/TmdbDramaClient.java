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

    private static final String BASE_SEARCH_URL = "https://api.themoviedb.org/3/search/tv";
    private static final String BASE_URL = "https://api.themoviedb.org/3/tv";

    public Integer getDramaTmdbIdByChineseName(String name) throws IOException {
        String tmdbApiKey = tmdbProperties.getKey();
        String query = URLEncoder.encode(name, "UTF-8");
        String requestUrl = BASE_SEARCH_URL + "?api_key=" + tmdbApiKey + "&language=zh-TW&query=" + query;

        try {
            JsonNode results = JsonNodeRequest.getJsonNodebyRequestQuery(requestUrl);
            if (results.isArray() && results.size() > 0) {
                JsonNode firstResult = results.get(0);
                Integer tmdbId = firstResult.path("id").asInt();
                return tmdbId;
            }
            return null;

        } catch (Exception e) {
            System.out.println("Exception Occurred!" + e.getMessage());
            e.printStackTrace();
            return null;
        }      
    }

    public ArrayList<Drama> fillDramaInfoByTmdbId(Integer tmdbId) throws IOException {
        String tmdbApiKey = tmdbProperties.getKey();
        String requestUrl = BASE_URL + "/" + tmdbId +"?api_key=" + tmdbApiKey + "&language=zh-TW";
        ArrayList <Drama> dramas = new ArrayList<Drama>();
        
        try {
            JsonNode results = JsonNodeRequest.getJsonNodebyRequestQuery(requestUrl);
            if (results.size() > 0) {
                // Fill in information from the fetched object
                JsonNode seasonNodes = results.path("seasons");

                for (int i = 0; i < seasonNodes.size(); i++) {
                    Drama drama = new Drama();
                    Integer seasonNumber = seasonNodes.get(i).path("season_number").asInt();
                    // A condition "original_country = 'KR'" added on 2025/8/16
                    if (seasonNumber > 0 && !(seasonNodes.path(i).path("air_date").isNull()) && (results.path("origin_country").path(0).asText().equals("KR"))) {
                        drama.setTmdbId(tmdbId);
                        drama.setIntroPageUrl("https://www.themoviedb.org/tv/" + results.path("id").asInt());
                        if (seasonNumber >= 2) {
                            drama.setChineseName(results.path("name").asText() + seasonNumber);
                            drama.setKoreanName(results.path("original_name").asText() + seasonNumber);
                        }
                        else {
                            drama.setChineseName(results.path("name").asText());
                            drama.setKoreanName(results.path("original_name").asText());
                        }

                        // Total number of episodes of a season
                        drama.setSeasonNumber(seasonNumber);
                        int totalNumberOfEps = results.path("seasons").get(i).path("episode_count").asInt();
                        drama.setTotalNumOfEps(totalNumberOfEps);

                        // Main Poster Url
                        drama.setMainPosterUrl("https://image.tmdb.org/t/p/original" + results.path("poster_path").asText());

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

                        dramas.add(drama);
                    }
                    else {
                        continue;
                    }

                }
                
                if (!dramas.isEmpty()) {
                    // Save the information of the drama to a .json file
                    String backupFilePath = "backup/drama_backup.json";
                    File backupFile = new File(backupFilePath);
                    backupFile.getParentFile().mkdirs();
                    objectMapper.writerWithDefaultPrettyPrinter().writeValue(backupFile, dramas);
                    
                    return dramas;
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

    public ArrayList<Drama> fillEngName(ArrayList<Drama> dramas) throws IOException {
        String tmdbApiKey = tmdbProperties.getKey();
        String requestUrl = BASE_URL + "/" + dramas.get(0).getTmdbId() +"?api_key=" + tmdbApiKey + "&language=en";

        try {
            JsonNode results = JsonNodeRequest.getJsonNodebyRequestQuery(requestUrl);
            if (results.size() > 0) {
                for (int i = 0; i < dramas.size(); i++) {
                    // Fill in the English information
                    if (dramas.get(i).getSeasonNumber() >= 2) {
                        dramas.get(i).setEnglishName(results.path("name").asText() + " " + dramas.get(i).getSeasonNumber());
                    }
                    else {
                        dramas.get(i).setEnglishName(results.path("name").asText());
                    }
                   
                }

                // Save the information of the drama to a .json file
                String backupFilePath = "backup/drama_backup.json";
                File backupFile = new File(backupFilePath);
                backupFile.getParentFile().mkdirs();
                objectMapper.writerWithDefaultPrettyPrinter().writeValue(backupFile, dramas);

                return dramas;
            }
            return null;

        } catch (Exception e) {
            System.out.println("Exception Occurred!" + e.getMessage());
            e.printStackTrace();
            return null;
        }      
    }

    public ArrayList<Drama> fillKrAgeRestriction(ArrayList<Drama> dramas) throws IOException {
        String tmdbApiKey = tmdbProperties.getKey();
        String requestUrl = BASE_URL + "/" + dramas.get(0).getTmdbId() +"/content_ratings?api_key=" + tmdbApiKey;

        try {
            JsonNode results = JsonNodeRequest.getJsonNodebyRequestQuery(requestUrl);
            if (results.size() > 0) {

                // Fill in the Korean Age Restriction
                if (results.isArray()) {
                    for (int i = 0; i < dramas.size(); i++) {
                        for (JsonNode node : results) {
                            if (node.path("iso_3166_1").asText().equals("KR")) {
                                dramas.get(i).setKrAgeRestriction(node.path("rating").asInt());
                            }
                        }
                    }
                }

                // Save the information of the drama to a .json file
                String backupFilePath = "backup/drama_backup.json";
                File backupFile = new File(backupFilePath);
                backupFile.getParentFile().mkdirs();
                objectMapper.writerWithDefaultPrettyPrinter().writeValue(backupFile, dramas);

                return dramas;
            }
            return null;

        } catch (Exception e) {
            System.out.println("Exception Occurred!" + e.getMessage());
            e.printStackTrace();
            return null;
        }      
    }

    public ArrayList<Drama> fillEpInfo(ArrayList<Drama> dramas) throws IOException {
        String tmdbApiKey = tmdbProperties.getKey();
        ArrayList<Drama> toRemove = new ArrayList<Drama>();

        try {
            for (Drama drama : dramas) {
                String requestUrl = BASE_URL + "/" + drama.getTmdbId() + "/season/" + drama.getSeasonNumber() + "?api_key=" + tmdbApiKey + "&language=zh-TW";
                JsonNode results = JsonNodeRequest.getJsonNodebyRequestQuery(requestUrl);
                if (results.size() > 0) {
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
                    
                    // Fill in other information
                    // 2025/8/16: Handle posters for different seasons
                    // 2025/8/19: Only update here again for dramas with more than one season
                    if (dramas.size() > 1) {
                        drama.setMainPosterUrl("https://image.tmdb.org/t/p/original" + results.path("poster_path").asText());
                    }

                }
                else {
                    toRemove.add(drama);
                }
            }

            // Remove dramas in toRemove
            dramas.removeAll(toRemove);

            // Save the information of the drama to a .json file
            String backupFilePath = "backup/drama_backup.json";
            File backupFile = new File(backupFilePath);
            backupFile.getParentFile().mkdirs();
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(backupFile, dramas);

            return dramas;

        } catch (Exception e) {
            System.out.println("Exception Occurred!" + e.getMessage() + "發生在 " + dramas);
            e.printStackTrace();
            return null;
        }   
    }

    public ArrayList<Drama> fillDramaStaff (ArrayList<Drama> dramas, String searchedActorName) throws IOException {
        String tmdbApiKey = tmdbProperties.getKey();
        ArrayList<Drama> toRemove = new ArrayList<Drama>();
        
        try {
            for (Drama drama : dramas) {
                String requestUrl = BASE_URL + "/" + drama.getTmdbId() + "/season/" + drama.getSeasonNumber() + "/credits?api_key=" + tmdbApiKey + "&language=zh-TW";

                JsonNode results = JsonNodeRequest.getJsonNodebyRequestQuery(requestUrl);
                if (results.size() > 0) {
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
                                toRemove.add(drama);
                            }
                        }
                    }
                }
                else {
                    toRemove.add(drama);
                }
            }

            // Remove dramas in toRemove
            dramas.removeAll(toRemove);

            // Save the information of the drama to a .json file
            String backupFilePath = "backup/drama_backup.json";
            File backupFile = new File(backupFilePath);
            backupFile.getParentFile().mkdirs();
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(backupFile, dramas);

            return dramas;

        } catch (Exception e) {
            System.out.println("Exception Occurred!" + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // Below methods (that return a single Drama object) are called while UPDATING a specific drama season

    public Drama fillDramaInfoByTmdbIdAndSeasonNumber(Integer tmdbId, Integer seasonNumber) throws IOException {
        String tmdbApiKey = tmdbProperties.getKey();
        String requestUrl = BASE_URL + "/" + tmdbId +"?api_key=" + tmdbApiKey + "&language=zh-TW";
        Drama drama = new Drama();
        
        try {
            JsonNode results = JsonNodeRequest.getJsonNodebyRequestQuery(requestUrl);
            if (results.size() > 0) {
                // Fill in information from the fetched object
                JsonNode selectedSeasonNode = null;
                JsonNode seasonNodes = results.path("seasons");
                for (JsonNode seasonNode : seasonNodes) {
                    if (seasonNode.path("season_number").asInt() == seasonNumber) {
                        selectedSeasonNode = seasonNode;
                        break;
                    }
                }
                if (!(selectedSeasonNode.path("air_date").isNull()) && (results.path("origin_country").path(0).asText().equals("KR"))) {
                    drama.setTmdbId(tmdbId);
                    drama.setIntroPageUrl("https://www.themoviedb.org/tv/" + results.path("id").asInt());
                    if (seasonNumber >= 2) {
                        drama.setChineseName(results.path("name").asText() + seasonNumber);
                        drama.setKoreanName(results.path("original_name").asText() + seasonNumber);
                    }
                    else {
                        drama.setChineseName(results.path("name").asText());
                        drama.setKoreanName(results.path("original_name").asText());
                    }

                    // Total number of episodes of a season
                    drama.setSeasonNumber(seasonNumber);
                    int totalNumberOfEps = selectedSeasonNode.path("episode_count").asInt();
                    drama.setTotalNumOfEps(totalNumberOfEps);

                    // Main Poster Url
                    drama.setMainPosterUrl("https://image.tmdb.org/t/p/original" + results.path("poster_path").asText());

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
            }
            return null;

        } catch (Exception e) {
            System.out.println("Exception Occurred!" + e.getMessage());
            e.printStackTrace();
            return null;
        }      
    }

    public Drama fillEngName(Drama drama) throws IOException {
        String tmdbApiKey = tmdbProperties.getKey();
        String requestUrl = BASE_URL + "/" + drama.getTmdbId() +"?api_key=" + tmdbApiKey + "&language=en";

        try {
            JsonNode results = JsonNodeRequest.getJsonNodebyRequestQuery(requestUrl);
            if (results.size() > 0) {
                // Fill in the English information
                if (drama.getSeasonNumber() >= 2) {
                    drama.setEnglishName(results.path("name").asText() + " " + drama.getSeasonNumber());
                }
                else {
                    drama.setEnglishName(results.path("name").asText());
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

    public Drama fillKrAgeRestriction(Drama drama) throws IOException {
        String tmdbApiKey = tmdbProperties.getKey();
        String requestUrl = BASE_URL + "/" + drama.getTmdbId() +"/content_ratings?api_key=" + tmdbApiKey;

        try {
            JsonNode results = JsonNodeRequest.getJsonNodebyRequestQuery(requestUrl);
            if (results.size() > 0) {

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

    public Drama fillEpInfo(Drama drama, boolean isMultipleSeasons) throws IOException {
        String tmdbApiKey = tmdbProperties.getKey();

        try {
            String requestUrl = BASE_URL + "/" + drama.getTmdbId() + "/season/" + drama.getSeasonNumber() + "?api_key=" + tmdbApiKey + "&language=zh-TW";
            JsonNode results = JsonNodeRequest.getJsonNodebyRequestQuery(requestUrl);
            if (results.size() > 0) {
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
                
                // Fill in other information
                // Update seasonal poster if the drama is part of a multi-season series
                if (isMultipleSeasons) {
                    drama.setMainPosterUrl("https://image.tmdb.org/t/p/original" + results.path("poster_path").asText());
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
            System.out.println("Exception Occurred!" + e.getMessage() + "發生在 " + drama);
            e.printStackTrace();
            return null;
        }   
    }

    public Drama fillDramaStaff (Drama drama) throws IOException {
        String tmdbApiKey = tmdbProperties.getKey();
        
        try {
            String requestUrl = BASE_URL + "/" + drama.getTmdbId() + "/season/" + drama.getSeasonNumber() + "/credits?api_key=" + tmdbApiKey + "&language=zh-TW";

            JsonNode results = JsonNodeRequest.getJsonNodebyRequestQuery(requestUrl);
            if (results.size() > 0) {
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

    public boolean isDramaFromTmdbByTmdbId (Integer tmdbId) throws IOException {
        String tmdbApiKey = tmdbProperties.getKey();
        String requestUrl = BASE_URL + "/" + tmdbId + "?api_key=" + tmdbApiKey + "&language=zh-TW";

        boolean isDrama = false;

        try {
            JsonNode results = JsonNodeRequest.getJsonNodebyRequestQuery(requestUrl);
            if (results.size() > 0) {
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

