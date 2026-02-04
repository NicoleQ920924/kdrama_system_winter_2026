package com.kdrama.backend.service;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
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

    private final AiService aiService;

    private final ObjectMapper objectMapper;
    private final TmdbProperties tmdbProperties;

    public TmdbActorClient(TmdbProperties tmdbProperties, ObjectMapper objectMapper, AiService aiService) {
        this.tmdbProperties = tmdbProperties;
        this.objectMapper = objectMapper;
        this.aiService = aiService;
    }

    @Autowired
    private MovieService movieService;

    @Autowired
    private DramaService dramaService;

    private static final String BASE_SEARCH_URL = "https://api.themoviedb.org/3/search/person";
    private static final String BASE_URL = "https://api.themoviedb.org/3/person";

    public Integer getActorTmdbIdByActorName(String name) throws IOException {
        String tmdbApiKey = tmdbProperties.getKey();
        String query = URLEncoder.encode(name, "UTF-8");
        String requestUrl = BASE_SEARCH_URL + "?api_key=" + tmdbApiKey + "&language=zh-TW&query=" + query;

        try {
            JsonNode results = JsonNodeRequest.getJsonNodebyRequestQuery(requestUrl);
            if (results != null && results.isArray() && results.size() > 0) {
                JsonNode firstResult = results.path(0);

                Integer tmdbId = firstResult.path("id").asInt();
                return tmdbId;
            }
            return null;

        } catch (Exception e) {
            System.out.println("Exception Occurred!" + e.getMessage());
            return null;
        }      
    }

    public Actor fillActorOtherInfo (Actor actor) throws IOException {
        String tmdbApiKey = tmdbProperties.getKey();
        if (actor == null) {
            return null;
        }

        try {
            String requestUrl = BASE_URL + "/" + actor.getTmdbId() +"?api_key=" + tmdbApiKey + "&language=zh-TW";
            JsonNode results = JsonNodeRequest.getJsonNodebyRequestQuery(requestUrl);
            if (results != null && !results.path("status_message").asText().equals("The resource you requested could not be found.")) {
                actor.setProfilePicUrl("https://media.themoviedb.org/t/p/w300_and_h450_bestv2/" + results.path("profile_path").asText());
                actor.setBirthday(results.path("birthday").asText());

                // Handling refined Chinese name for further work fetching
                actor.setChineseName(results.path("name").asText());
                
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
        if (actor == null) {
            return null;
        }

        try {
            // English Name
            String requestUrl = BASE_URL + "/" + actor.getTmdbId() +"?api_key=" + tmdbApiKey + "&language=en";
            JsonNode results = JsonNodeRequest.getJsonNodebyRequestQuery(requestUrl);
            if (results != null && !results.path("status_message").asText().equals("The resource you requested could not be found.")) {
                // Fill in the English information
                actor.setEnglishName(results.path("name").asText());
            }

            // Korean Name
            requestUrl = BASE_URL + "/" + actor.getTmdbId() +"?api_key=" + tmdbApiKey + "&language=kr";
            results = JsonNodeRequest.getJsonNodebyRequestQuery(requestUrl);
            if (results != null && !results.path("status_message").asText().equals("The resource you requested could not be found.")) {
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
        if (actor == null) {
            return null;
        }

        try {
            String requestUrl = BASE_URL + "/" + actor.getTmdbId() +"/combined_credits?api_key=" + tmdbApiKey + "&language=zh-TW";
            JsonNode results = JsonNodeRequest.getJsonNodebyRequestQuery(requestUrl);
            if (results != null && !results.path("status_message").asText().equals("The resource you requested could not be found.")) {
                JsonNode workNodes = results.path("cast");

                // Also fetch actor's works with TMDB API
                // Get the drama_id and movie_id from database
                // Save them, and also save them as actor's dramas and movies if not saved yet
                if (workNodes.isArray() && workNodes.size() > 0) {
                    for (JsonNode workNode : workNodes) {
                        String mediaType = workNode.path("media_type").asText();
                        Integer tmdbWorkId = workNode.path("id").asInt();

                        if (mediaType.equals("movie")) {
                            Movie movie = new Movie();
                            Movie savedMovie = null;

                            Optional<Movie> optionalMovie = movieService.getMovieByTmdbId(tmdbWorkId);

                            if (includesExistingWork && !optionalMovie.isEmpty()) { // Update existing work as well
                                movie.setTmdbId(tmdbWorkId);
                                movie.setChineseName(workNode.path("title").asText());
                                Movie filledMovie = movieService.fillMovieMoreInfo(movie);
                                if (filledMovie != null) {
                                    filledMovie = aiService.aiUpdateMovieInfo(filledMovie);
                                    filledMovie = movieService.fillTWPlatformInformation(filledMovie);
                                    Movie existingMovie = optionalMovie.get();
                                    if (filledMovie != null) {
                                    savedMovie = movieService.updateMovie(existingMovie.getMovieId(), filledMovie, true);
                                    } else {
                                        System.out.println("Movie fetch failed or skipped for tmdbId: " + tmdbWorkId);
                                        savedMovie = null;
                                    }
                                }
                                else {
                                    System.out.println("Movie fetch failed or skipped for tmdbId: " + tmdbWorkId);
                                    continue;
                                }
                            }
                            else { // Only add movies not in the database
                                savedMovie = optionalMovie.orElseGet(() -> {
                                    movie.setTmdbId(tmdbWorkId);
                                    movie.setChineseName(workNode.path("title").asText());
                                    Movie filledMovie = movieService.fillMovieMoreInfo(movie);
                                    if (filledMovie != null) {
                                        filledMovie = aiService.aiUpdateMovieInfo(filledMovie);
                                        filledMovie = movieService.fillTWPlatformInformation(filledMovie);
                                         if (filledMovie != null) {
                                            return movieService.saveMovie(filledMovie);
                                        } else {
                                            System.out.println("Movie fetch failed or skipped for tmdbId: " + tmdbWorkId);
                                            return null;
                                        }
                                    }
                                    else {
                                        System.out.println("Movie fetch failed or skipped for tmdbId: " + tmdbWorkId);
                                        return null;
                                    }
                                   
                                });
                            }
                            if (savedMovie != null) {
                                try {
                                    if (!actor.getMovies().contains(savedMovie) && savedMovie.getLeadActors().contains(actor.getChineseName())) {
                                        actor.getMovies().add(savedMovie);
                                    }
                                } catch (Exception e) {
                                    System.out.println("Exception occurred at movie: " + savedMovie.getChineseName());
                                    e.printStackTrace();
                                }
                                
                            }
                        }
                        else if (mediaType.equals("tv")) {
                            if (!dramaService.isDrama(tmdbWorkId)) {
                                continue;
                            }

                            Integer totalSeasons = dramaService.getDramaSeasonCount(tmdbWorkId);
    
                            for (int season = 1; season <= totalSeasons; season++) {
                                Drama drama = new Drama();
                                Drama filledDrama = null;

                                drama.setTmdbId(tmdbWorkId);
                                drama.setSeasonNumber(season);
                                drama.setChineseName(workNode.path("name").asText() + " - 第" + season + "季");

                                filledDrama = dramaService.fillDramaMoreInfo(drama, actor.getChineseName());

                                if (filledDrama != null) {
                                    filledDrama = aiService.aiUpdateDramaInfo(filledDrama);
                                    filledDrama = dramaService.fillTWPlatformInformation(filledDrama);
                                    Optional<Drama> existingDramaOpt = dramaService.getDramaByTmdbIdAndSeasonNumber(tmdbWorkId, season);
                                    if (existingDramaOpt.isPresent()) {
                                        if (!includesExistingWork) {
                                            continue; // Skip if not including updating existing works
                                        }
                                        Drama existingDrama = existingDramaOpt.get();
                                        if (filledDrama != null) {
                                            drama = dramaService.updateDrama(existingDrama.getDramaId(), filledDrama, false);
                                        } else {
                                            System.out.println("Drama fetch failed or skipped for tmdbId: " + tmdbWorkId + ", season: " + season);
                                            drama = null;
                                        }
                                    }
                                    else {
                                        if (filledDrama != null) {
                                            drama = dramaService.saveDrama(filledDrama);
                                        } else {
                                            System.out.println("Drama fetch failed or skipped for tmdbId: " + tmdbWorkId + ", season: " + season);
                                        }
                                    }
                                }
                                else {
                                    System.out.println("Drama fetch failed or skipped for tmdbId: " + tmdbWorkId + ", season: " + season);
                                    continue;
                                }

                                if (drama != null) {
                                    // Initialization of sizes to prevent LazyInitializationException
                                    try {
                                        if (!drama.equals(null)) {
                                            drama.getGenres().size();
                                            drama.getNetworks().size();
                                            drama.getDramaTwPlatformMap().size();
                                            drama.getLeadActors().size();
                                            drama.getDirectorNames().size();
                                            drama.getScriptwriterNames().size();
                                            // Only establish new relationships if: 
                                            // 1. The relationship between the drama and the actor has not been established yet
                                            // 2. The list of lead actors of the drama does include this actor (previously handled by TmdbDramaClient.java for new dramas, now here we need to handle existing dramas)
                                            if (!actor.getDramas().contains(drama) && drama.getLeadActors().contains(actor.getChineseName())) {
                                                actor.getDramas().add(drama);
                                            }   
                                        }
                                    } catch (Exception e) {
                                        System.out.println("Exception occurred at drama: " + drama.getChineseName());
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
