package com.kdrama.backend.service;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kdrama.backend.config.TmdbProperties;
import com.kdrama.backend.enums.ActorGender;
import com.kdrama.backend.model.*;
import com.kdrama.backend.util.JsonNodeRequest;

import jakarta.transaction.Transactional;

@Component
public class TmdbActorClient {

    private final ObjectMapper objectMapper;
    private final TmdbProperties tmdbProperties;

    public TmdbActorClient(TmdbProperties tmdbProperties, ObjectMapper objectMapper) {
        this.tmdbProperties = tmdbProperties;
        this.objectMapper = objectMapper;
    }

    @Autowired
    private MovieService movieService;

    @Autowired
    private DramaService dramaService;

    private static final String BASE_SEARCH_URL = "https://api.themoviedb.org/3/search/person";
    private static final String BASE_URL = "https://api.themoviedb.org/3/person";

    public Integer getActorTmdbIdByChineseName(String name) throws IOException {
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
            return null;
        }      
    }

    public Actor fillActorInfoByTmdbId (Integer tmdbId) throws IOException {
        String tmdbApiKey = tmdbProperties.getKey();
        String requestUrl = BASE_URL + "/" + tmdbId +"?api_key=" + tmdbApiKey + "&language=zh-TW";

        try {
            JsonNode results = JsonNodeRequest.getJsonNodebyRequestQuery(requestUrl);
            if (results.size() > 0) {
                Actor actor = new Actor();
                actor.setTmdbId(tmdbId);
                actor.setChineseName(results.path("name").asText()); // The same as the search query as for now
                actor.setProfilePicUrl("https://media.themoviedb.org/t/p/w300_and_h450_bestv2/" + results.path("profile_path").asText());
                actor.setBirthday(results.path("birthday").asText());
                String biography = results.path("biography").asText();
                if (!biography.isEmpty()) {
                    if (biography.length() > 600) {
                        actor.setBiography(biography.substring(0, 600) + "...");
                    }
                    else {
                        actor.setBiography(biography);
                    }                    
                }
                else {
                    actor.setBiography(results.path("biography").asText());
                }

                // Handling actorGender
                String genderIndex = results.path("gender").asText();
                if (genderIndex.equals("1")) {
                    actor.setActorGender(ActorGender.FEMALE);
                }
                else if (genderIndex.equals("2")) {
                    actor.setActorGender(ActorGender.MALE);
                }

                // Save the information of the actor to a .json file         
                String backupFilePath = "backup/actor_backup.json";
                File backupFile = new File(backupFilePath);
                backupFile.getParentFile().mkdirs();
                objectMapper.writerWithDefaultPrettyPrinter().writeValue(backupFile, actor);

                return actor;
            }
            return null;

        } catch (Exception e) {
            System.out.println("Exception Occurred!" + e.getMessage());
            return null;
        }
    }

    public Actor fillNames(Actor actor) throws IOException {
        String tmdbApiKey = tmdbProperties.getKey();
        try {
            // English Name
            String requestUrl = BASE_URL + "/" + actor.getTmdbId() +"?api_key=" + tmdbApiKey + "&language=en";
            JsonNode results = JsonNodeRequest.getJsonNodebyRequestQuery(requestUrl);
            if (results.size() > 0) {
                // Fill in the English information
                actor.setEnglishName(results.path("name").asText());
            }

            // Korean Name
            requestUrl = BASE_URL + "/" + actor.getTmdbId() +"?api_key=" + tmdbApiKey + "&language=kr";
            results = JsonNodeRequest.getJsonNodebyRequestQuery(requestUrl);
            if (results.size() > 0) {
                // Fill in the Korean information
                actor.setKoreanName(results.path("name").asText());

                // Save the information of the actor to a .json file
                String backupFilePath = "backup/actor_backup.json";
                File backupFile = new File(backupFilePath);
                backupFile.getParentFile().mkdirs();
                objectMapper.writerWithDefaultPrettyPrinter().writeValue(backupFile, actor);

                return actor;
            }
            return null;

        } catch (Exception e) {
            System.out.println("Exception Occurred!" + e.getMessage());
            return null;
        }      
    }
    
    @Transactional // To keep hibernate session awake
    public Actor fillActorWorks (Actor actor, boolean includesExistingWork) throws IOException {
        String tmdbApiKey = tmdbProperties.getKey();
        String requestUrl = BASE_URL + "/" + actor.getTmdbId() +"/combined_credits?api_key=" + tmdbApiKey + "&language=zh-TW";

        try {
            JsonNode results = JsonNodeRequest.getJsonNodebyRequestQuery(requestUrl);
            if (results.size() > 0) {
                JsonNode workNodes = results.path("cast");

                // Also fetch actor's works with TMDB API
                // Get the drama_id and movie_id from database
                // Save them, and also save them as actor's dramas and movies if not saved yet
                if (workNodes.isArray() && workNodes.size() > 0) {
                    for (JsonNode workNode : workNodes) {
                        String mediaType = workNode.path("media_type").asText();
                        Integer tmdbWorkId = workNode.path("id").asInt();

                        Optional<Movie> optionalMovie = movieService.getMovieByTmdbId(tmdbWorkId);

                        if (mediaType.equals("movie")) {
                            Movie movie = new Movie();

                            if (includesExistingWork && !optionalMovie.isEmpty()) { // Update existing work as well
                                Movie fetched = movieService.getMovieFromTmdbByTmdbId(tmdbWorkId);
                                Movie existingMovie = optionalMovie.get();
                                if (fetched != null) {
                                   movie = movieService.updateMovie(existingMovie.getMovieId(), fetched, true);
                                } else {
                                    System.out.println("Movie fetch failed or skipped for tmdbId: " + tmdbWorkId);
                                    movie = null;
                                }
                            }
                            else { // Only add movies not in the database
                                movie = optionalMovie.orElseGet(() -> {
                                    Movie fetched = movieService.getMovieFromTmdbByTmdbId(tmdbWorkId);
                                    if (fetched != null) {
                                        return movieService.saveMovie(fetched);
                                    } else {
                                        System.out.println("Movie fetch failed or skipped for tmdbId: " + tmdbWorkId);
                                        return null;
                                    }
                                });
                            }
                            if (movie != null) {
                                try {
                                    actor.getMovies().add(movie);
                                } catch (Exception e) {
                                    System.out.println("Exception occurred at movie: " + movie.getChineseName());
                                    e.printStackTrace();
                                }
                                
                            }
                        }
                        else if (mediaType.equals("tv")) {
                            List<Drama> dramas = dramaService.getDramasByTmdbId(tmdbWorkId);
    
                            if (dramas == null || dramas.isEmpty() || includesExistingWork) {
                                // Check if the drama is valid before saving
                                if (dramaService.isDrama(tmdbWorkId)) {
                                    List<Drama> fetched = dramaService.getDramasFromTmdbByTmdbId(tmdbWorkId, actor.getChineseName());
                                    if (includesExistingWork && !dramas.isEmpty() && fetched != null && !fetched.isEmpty()) {
                                        dramas = dramaService.updateDramaAllSeasons(dramas, fetched);
                                    }
                                    else if (fetched != null && !fetched.isEmpty()) {
                                        dramas = dramaService.saveDramaAllSeasons(fetched);
                                    }
                                }
                            }

                            if (dramas != null) {
                                for (Drama d : dramas) {
                                    // Initialization of sizes to prevent LazyInitializationException
                                    try {
                                        if (!d.equals(null)) {
                                            d.getGenres().size();
                                            d.getNetworks().size();
                                            d.getDramaTwPlatformMap().size();
                                            d.getLeadActors().size();
                                            d.getDirectorNames().size();
                                            d.getScriptwriterNames().size();

                                            // Only establish new relationships if: 
                                            // 1. The relationship between the drama and the actor has not been established yet
                                            // 2. The list of lead actors of the drama does include this actor (previously handled by TmdbDramaClient.java for new dramas, now here we need to handle existing dramas)
                                            if (!actor.getDramas().contains(d) && d.getLeadActors().contains(actor.getChineseName())) {
                                                actor.getDramas().add(d);
                                            }   
                                        }
                                    } catch (Exception e) {
                                        System.out.println("Exception occurred at drama: " + d.getChineseName());
                                        e.printStackTrace();
                                    }
                                    
                                }
                            }
                        }
                    }
                }
                

                // Save the information of the actor to a .json file
                String backupFilePath = "backup/actor_backup.json";
                File backupFile = new File(backupFilePath);
                backupFile.getParentFile().mkdirs();
                objectMapper.writerWithDefaultPrettyPrinter().writeValue(backupFile, actor);

                return actor;
            }
            return null;

        } catch (Exception e) {
            System.out.println("Exception Occurred!" + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
