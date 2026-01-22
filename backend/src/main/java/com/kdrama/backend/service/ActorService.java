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

    private final ObjectMapper objectMapper;

    public ActorService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    // Refer to ActorRepository.java for the CRUD repository methods

    // C1-1: Call TMDB API to fetch actor information
    public Actor getActorFromTmdbByChineseName(String name) {
        try {
            Integer tmdb = tmdbActorClient.getActorTmdbIdByChineseName(name);
            Actor fetchedActor = getActorFromTmdbByTmdbId(tmdb, false);
            return fetchedActor;
		} catch (Exception e) {
			System.err.println("Exception Occurred!" + e.getMessage());
            return null;
		}
    }

    // C1-2: Call TMDB API to fetch actor information (Also the entrance to update via API)
    public Actor getActorFromTmdbByTmdbId(Integer tmdbId, boolean includesExistingWork) {
        try {
			Actor fetchedActor = tmdbActorClient.fillActorInfoByTmdbId(tmdbId);
            tmdbActorClient.fillNames(fetchedActor);
            tmdbActorClient.fillActorWorks(fetchedActor, includesExistingWork);
            fetchedActor.setLastUpdatedByApi(LocalDateTime.now());

            String backupFilePath = "backup/actor_backup.json";
            File backupFile = new File(backupFilePath);
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(backupFile, fetchedActor);
            System.out.println("Successfully save file: " + backupFile);

            return fetchedActor;
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

    // R3: Get an actor identified by Chinese name
    public Optional<Actor> getActorByChineseName(String name) {
        return actorRepository.findByChineseName(name);
    }

    // U: Update an actor
    public Actor updateActor(@PathVariable Integer id, @RequestBody Actor updatedActor, boolean apiMode) {
        return actorRepository.findById(id)
                .map(actor -> {
                    actor.setTmdbId(updatedActor.getTmdbId());
                    actor.setChineseName(updatedActor.getChineseName());
                    actor.setEnglishName(updatedActor.getEnglishName());
                    actor.setKoreanName(updatedActor.getKoreanName());
                    actor.setProfilePicUrl(updatedActor.getProfilePicUrl());
                    actor.setActorGender(updatedActor.getActorGender());
                    actor.setBirthday(updatedActor.getBirthday());
                    if (actor.getBiography().isEmpty() && apiMode || !apiMode) {
                        actor.setBiography(updatedActor.getBiography());
                    }
                    actor.setDramas(updatedActor.getDramas());
                    actor.setMovies(updatedActor.getMovies());
                    if (!apiMode) {
                        actor.setNamuWikiPageUrl(updatedActor.getNamuWikiPageUrl());
                    }
                    actor.setLastUpdatedByApi(updatedActor.getLastUpdatedByApi());
                    if (actor.isManuallyEdited() == false) {
                        actor.setManuallyEdited(updatedActor.isManuallyEdited());
                    }
                    return actorRepository.save(actor);
                })
                .orElseGet(() -> {
                    updatedActor.setActorId(id);
                    return actorRepository.save(updatedActor);
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
