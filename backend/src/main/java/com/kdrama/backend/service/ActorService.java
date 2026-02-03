package com.kdrama.backend.service;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kdrama.backend.model.Actor;
import com.kdrama.backend.repository.ActorRepository;

@Service
public class ActorService {
    @Autowired
    private TmdbActorClient tmdbActorClient;

    @Autowired
    private ActorRepository actorRepository;

    private final AiService aiService;

    private final ObjectMapper objectMapper;

    public ActorService(ObjectMapper objectMapper, AiService aiService) {
        this.objectMapper = objectMapper;
        this.aiService = aiService;
    }

    // Refer to ActorRepository.java for the CRUD repository methods

    // C1-1: Fetch actor information (TMDB ID)
    // Return back to ActorController to check if actor exists in database
    public Actor fillActorBasicInfo(String name) {
        try {
            Actor actor = new Actor();
            actor.setChineseName(name);
            Integer tmdbId = tmdbActorClient.getActorTmdbIdByActorName(actor.getChineseName());
            if (tmdbId == null) {
                tmdbId = aiService.aiGetActorTmdbId(actor.getChineseName());
            }
            actor.setTmdbId(tmdbId);
            return actor; 
		} catch (Exception e) {
			System.err.println("Exception Occurred!" + e.getMessage());
            return null;
		}
    }

    // C1-2: Call TMDB API to fetch actor information
    public Actor fillActorMoreInfo(Actor actor, boolean includesExistingWork) {
        try {
			actor = tmdbActorClient.fillActorOtherInfo(actor);
            actor = tmdbActorClient.fillNames(actor);
            actor = tmdbActorClient.fillActorWorks(actor, includesExistingWork);
            actor.setLastUpdatedByApi(LocalDateTime.now());

            String backupFilePath = "backup/actor_backup.json";
            File backupFile = new File(backupFilePath);
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(backupFile, actor);
            System.out.println("Successfully save file: " + backupFile);

            return actor;
		} catch (Exception e) {
			System.err.println("Exception Occurred!" + e.getMessage());
            return null;
		}
    }
   
    // C2: Save an actor
    public Actor saveActor(@RequestBody Actor actor) {
        return actorRepository.save(actor);
    }

    // R1: Get information of all the actors
    public List<Actor> getAllActors() {
        return actorRepository.findAll();
    }

    // R2: Get information of an actor identified by id
    public Optional<Actor> getActorById(@PathVariable Integer id) {
        return actorRepository.findById(id);
    }

    // R3: Get an actor identified by TMDB ID
    public Optional<Actor> getActorByTmdbId(Integer tmdbId) {
        return actorRepository.findByTmdbId(tmdbId);
    }

    // R4: Get an actor identified by Chinese name
    public Optional<Actor> getActorByChineseName(String name) {
        return actorRepository.findByChineseName(name);
    }

    // U: Update an actor
    public Actor updateActor(@PathVariable Integer id, @RequestBody Actor actorToUpdate, boolean apiMode) {
        return actorRepository.findById(id)
                .map(actor -> {
                    actor.setTmdbId(actorToUpdate.getTmdbId());
                    actor.setChineseName(actorToUpdate.getChineseName());
                    actor.setEnglishName(actorToUpdate.getEnglishName());
                    actor.setKoreanName(actorToUpdate.getKoreanName());
                    actor.setProfilePicUrl(actorToUpdate.getProfilePicUrl());
                    actor.setActorGender(actorToUpdate.getActorGender());
                    actor.setBirthday(actorToUpdate.getBirthday());
                    if (!apiMode) {
                        actor.setBiography(actorToUpdate.getBiography());
                    }
                    actor.setDramas(actorToUpdate.getDramas());
                    actor.setMovies(actorToUpdate.getMovies());
                    if (!apiMode) {
                        actor.setInstagramPageUrl(actorToUpdate.getInstagramPageUrl());
                    }
                    if (!apiMode) {
                        actor.setChineseWikipediaPageUrl(actorToUpdate.getChineseWikipediaPageUrl());
                    }
                    if (!apiMode) {
                        actor.setNamuWikiPageUrl(actorToUpdate.getNamuWikiPageUrl());
                    }
                    actor.setLastUpdatedByApi(actorToUpdate.getLastUpdatedByApi());
                    if (actor.isAiOrManuallyEdited() == false) {
                        actor.setAiOrManuallyEdited(actorToUpdate.isAiOrManuallyEdited());
                    }
                    return actorRepository.save(actor);
                })
                .orElseGet(() -> {
                    actorToUpdate.setActorId(id);
                    return actorRepository.save(actorToUpdate);
                });
    }

    // D: Delete an actor
    public void deleteActor(@PathVariable Integer id) {
        actorRepository.deleteById(id);
    }

    // D-Bonus: Check if an actor exists in database before deletion
    public boolean actorExists(@PathVariable Integer id) {
        return actorRepository.existsById(id);
    }
}
