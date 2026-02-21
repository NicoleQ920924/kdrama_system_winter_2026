package com.kdrama.backend.service;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kdrama.backend.config.TmdbProperties;
import com.kdrama.backend.model.Movie;
import com.kdrama.backend.util.JsonNodeRequest;

@Component
public class TmdbMovieClient {

    private final ObjectMapper objectMapper;
    private final TmdbProperties tmdbProperties;

    public TmdbMovieClient(TmdbProperties tmdbProperties, ObjectMapper objectMapper) {
        this.tmdbProperties = tmdbProperties;
        this.objectMapper = objectMapper;
    }

    private static final String BASE_SEARCH_URL = "https://api.themoviedb.org/3/search/movie";
    private static final String BASE_URL = "https://api.themoviedb.org/3/movie";

    public Integer getMovieTmdbIdByMovieName(String chineseName) throws IOException {
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

    public Movie fillMovieOtherInfo(Movie movie) throws IOException {
        String tmdbApiKey = tmdbProperties.getKey();
        if (movie == null) {
            return null;
        }

        try {
            String requestUrl = BASE_URL + "/" + movie.getTmdbId() + "?api_key=" + tmdbApiKey + "&language=zh-TW";
            JsonNode results = JsonNodeRequest.getJsonNodebyRequestQuery(requestUrl);
            
            if (results != null && !results.path("status_message").asText().equals("The resource you requested could not be found.")) {
                // Fill in additional information from the fetched object
                if (results.path("original_language").asText().equals("ko")) {
                    // Genres
                    List<String> genres = new ArrayList<String>();
                    JsonNode genreNodes = results.path("genres");
                    if (genreNodes.isArray()) {
                        for (JsonNode node : genreNodes) {
                            String genre = node.path("name").asText();
                            if (genre.equals("Sci-Fi & Fantasy")) {
                                genre = "奇幻&科幻";
                            }
                            genres.add(genre);
                        }
                        movie.setGenres(genres);
                    }

                    // Others
                    movie.setMainPosterUrl("https://image.tmdb.org/t/p/original" + results.path("poster_path").asText());
                    movie.setIntroPageUrl("https://www.themoviedb.org/movie/" + results.path("id").asInt());
                    movie.setTotalRuntime(results.path("runtime").asInt());
                    movie.setReleaseDate(results.path("release_date").asText());

                    // Save the information of the movie to a .json file
                    String backupFilePath = "backup/movie_backup.json";
                    File backupFile = new File(backupFilePath);
                    backupFile.getParentFile().mkdirs();
                    objectMapper.writerWithDefaultPrettyPrinter().writeValue(backupFile, movie);

                    return movie;
                }
                return null;
            }
            return null;

        } catch (Exception e) {
            System.out.println("Exception Occurred!" + e.getStackTrace());
            return null;
        }      
    }

    public Movie fillKrAgeRestriction(Movie movie) throws IOException {
        String tmdbApiKey = tmdbProperties.getKey();
        if (movie == null) {
            return null;
        }

        try {
            String requestUrl = BASE_URL + "/" + movie.getTmdbId() + "/release_dates?api_key=" + tmdbApiKey;
            JsonNode results = JsonNodeRequest.getJsonNodebyRequestQuery(requestUrl);
            if (results != null && !results.path("status_message").asText().equals("The resource you requested could not be found.")) {
                // Fill in the Korean Age Restriction
                if (results.isArray()) {
                    for (JsonNode node : results) {
                        if (node.path("iso_3166_1").asText().equals("KR")) {
                            movie.setKrAgeRestriction(node.path("release_dates").get(0).path("certification").asInt());
                        }
                    }
                }

                // Save the information of the movie to a .json file
                String backupFilePath = "backup/movie_backup.json";
                File backupFile = new File(backupFilePath);
                backupFile.getParentFile().mkdirs();
                objectMapper.writerWithDefaultPrettyPrinter().writeValue(backupFile, movie);

                return movie;
            }
            return null;

        } catch (Exception e) {
            System.out.println("Exception Occurred!" + e.getStackTrace());
            return null;
        }      
    }

    public Movie fillMovieStaff (Movie movie) throws IOException {
        String tmdbApiKey = tmdbProperties.getKey();
        if (movie == null) {
            return null;
        }

        try {
            String requestUrl = BASE_URL + "/" + movie.getTmdbId() + "/credits?api_key=" + tmdbApiKey + "&language=zh-TW";
            JsonNode results = JsonNodeRequest.getJsonNodebyRequestQuery(requestUrl);
            if (results != null && !results.path("status_message").asText().equals("The resource you requested could not be found.")) {
                // Fill in additional information from the fetched object
                // Director and Scriptwriter Names
                List<String> directorNames = new ArrayList<String>();
                List<String> scriptwriterNames = new ArrayList<String>();
                JsonNode crewNodes = results.path("crew");
                if (crewNodes.isArray()) {
                    for (JsonNode node : crewNodes) {
                        String job = node.path("job").asText();
                        if (job.equals("Director")) {
                            directorNames.add(node.path("name").asText());
                        }
                        else if (job.equals("Writer")) {
                            scriptwriterNames.add(node.path("name").asText());
                        }
                    }
                    movie.setDirectorNames(directorNames);
                    movie.setScriptwriterNames(scriptwriterNames);
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
                    movie.setLeadActors(castNames);
                }

                // Save the information of the movie to a .json file
                String backupFilePath = "backup/movie_backup.json";
                File backupFile = new File(backupFilePath);
                backupFile.getParentFile().mkdirs();
                objectMapper.writerWithDefaultPrettyPrinter().writeValue(backupFile, movie);

                return movie;
            }
            return null;

        } catch (Exception e) {
            System.out.println("Exception Occurred!" + e.getStackTrace());
            return null;
        }
    }
}
