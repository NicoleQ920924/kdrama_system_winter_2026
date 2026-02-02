package com.kdrama.backend.service;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kdrama.backend.model.Actor;
import com.kdrama.backend.model.Drama;
import com.kdrama.backend.repository.DramaRepository;
import com.kdrama.backend.repository.ActorRepository;

import jakarta.transaction.Transactional;

@Service
public class DramaService {

    @Autowired
    private TmdbDramaClient tmdbDramaClient;

    @Autowired
    private DramaRepository dramaRepository;

    @Autowired
    private ActorRepository actorRepository;

    @Autowired
    private TWOTTPlatformScraper platformScraper;

    @Autowired
    private TmdbPlatformClient tmdbPlatformClient;

    private final AiService aiService;

    private final ObjectMapper objectMapper;

    public DramaService(ObjectMapper objectMapper, AiService aiService) {
        this.aiService = aiService;
        this.objectMapper = objectMapper;
    }

    // Refer to DramaRepository.java for the CRUD repository methods

    // C1-1: Fetch drama information (TMDB ID and season number)
    // Return back to DramaController to check if drama exists in database
    public Drama fillDramaBasicInfo(String chineseName) {
        try {
            Drama drama = new Drama();
            drama.setChineseName(chineseName);
            Integer seasonNumber = aiService.aiGetDramaSeasonNumber(drama.getChineseName());
            drama.setSeasonNumber(seasonNumber);
            Integer tmdbId = tmdbDramaClient.getDramaTmdbIdByDramaName(drama.getChineseName());
            if (tmdbId == null) {
                tmdbId = aiService.aiGetDramaTmdbId(drama.getChineseName());
            }
            drama.setTmdbId(tmdbId);
            return drama;     
		} catch (Exception e) {
			System.err.println("Exception Occurred!" + e.getMessage());
            e.printStackTrace();
            return null;
		}
    }

    // C1-2: Call TMDB API to fetch drama information
    // searchedActorName is passed by TmdbActorClient.java
    public Drama fillDramaMoreInfo(Drama drama, String searchedActorName) {
        try {
            drama = tmdbDramaClient.fillDramaOtherInfo(drama);
            drama = tmdbDramaClient.fillDramaSeasonalInfo(drama);
            drama = tmdbDramaClient.fillKrAgeRestriction(drama);
            drama = tmdbDramaClient.fillDramaStaff(drama, searchedActorName);
            drama = fillTWPlatformInformation(drama);
            return drama;     
		} catch (Exception e) {
			System.err.println("Exception Occurred!" + e.getMessage());
            e.printStackTrace();
            return null;
		}
    }
    
    // C1-3: Fill TW OTT Platform Information (parameter: ArrayList<Drama>)
    public Drama fillTWPlatformInformation(Drama fetchedDrama) {
        try {
            if (fetchedDrama == null) {
                System.err.println("fetchedDrama is null, so platform information cannot be filled!");
                return fetchedDrama;
            }

            try {
                Map<String, String> platformInfoMap = platformScraper.getWorkTWOTTPlatformInfo(fetchedDrama.getChineseName(), "drama");
                fetchedDrama.setDramaTwPlatformMap(platformInfoMap);
                platformInfoMap.putAll(tmdbPlatformClient.getIntlPlatformInfoByWorkTmdbId(fetchedDrama.getTmdbId(), fetchedDrama.getSeasonNumber(), "drama"));
                System.out.println(fetchedDrama.getChineseName() + ": " + platformInfoMap.keySet());
                fetchedDrama.setLastUpdatedByApi(LocalDateTime.now());
            } catch (Exception innerE) {
                System.err.println("Failed to save platform information of " + fetchedDrama.getChineseName() + " due to" + innerE.getMessage());
                innerE.printStackTrace();
            }

            String backupFilePath = "backup/drama_backup.json";
            File backupFile = new File(backupFilePath);
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(backupFile, fetchedDrama);
            System.out.println("Successfully save file: " + backupFile);

            return fetchedDrama;

        } catch (Exception e) {
            System.err.println("Exception Occurred!" + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
   
    // C2: Save a drama
    public Drama saveDrama(@RequestBody Drama drama) {
        return dramaRepository.save(drama);
    }

    // C-Bonus: Check if the TV Show is a drama
    public boolean isDrama (@RequestBody Integer tmdbId) {
        try {
            return tmdbDramaClient.isDramaFromTmdbByTmdbId(tmdbId);
        }
        catch (Exception e) {
			System.err.println("Exception Occurred!" + e.getMessage());
            return false;
		}
    }

    // C-Bonus: Check if a drama has multiple seasons (to check if new seasonal posters are necessary)
    public boolean isMultipleSeasons(Drama drama) {
        Integer tmdbId = drama.getTmdbId();
        List<Drama> fetchedDramas = getDramasByTmdbId(tmdbId);
        if (drama.getSeasonNumber() > 1 || fetchedDramas.size() > 1) {
            return true;
        }
        else {
            return false;
        }
    }

    // R1: Get information of all the dramas
    public List<Drama> getAllDramas() {
        return dramaRepository.findAll();
    }

    // R2: Get information of a drama identified by id
    public Optional<Drama> getDramaById(@PathVariable Integer id) {
        return dramaRepository.findById(id);
    }

    // R3: Get information of dramas identified by tmdbId
    public List<Drama> getDramasByTmdbId(@PathVariable Integer id) {
        return dramaRepository.findByTmdbId(id);
    }

    // R4: Get information of a drama identified by tmdbId and season number
    public Optional<Drama> getDramaByTmdbIdAndSeasonNumber(@PathVariable Integer tmdbId, @PathVariable Integer seasonNumber) {
        return dramaRepository.findByTmdbIdAndSeasonNumber(tmdbId, seasonNumber);
    }

    // R5: Get information of dramas identified by Chinese title
    public Optional<List<Drama>> getDramasByChineseName(@PathVariable String chineseName) {
        return dramaRepository.findAllByChineseName(chineseName);
    }

    // R6: Get information of a drama identified by Chinese title
    public Optional<Drama> getDramaByChineseName(@PathVariable String chineseName) {
        return dramaRepository.findByChineseName(chineseName);
    }

    // U: Update a drama
    public Drama updateDrama(@PathVariable Integer id, @RequestBody Drama dramaToUpdate, boolean apiMode) {
        return dramaRepository.findById(id)
                .map(drama -> {
                    drama.setTmdbId(dramaToUpdate.getTmdbId());
                    drama.setSeasonNumber(dramaToUpdate.getSeasonNumber());
                    if ((drama.getChineseName().isEmpty() && apiMode) || !apiMode) {
                        drama.setChineseName(dramaToUpdate.getChineseName());
                    }
                    if ((drama.getEnglishName().isEmpty() && apiMode) || !apiMode) {
                        drama.setEnglishName(dramaToUpdate.getEnglishName());
                    }
                    if ((drama.getKoreanName().isEmpty() && apiMode) || !apiMode) {
                        drama.setKoreanName(dramaToUpdate.getKoreanName());
                    }
                    drama.setTotalNumOfEps(dramaToUpdate.getTotalNumOfEps());
                    drama.setCurrentEpNo(dramaToUpdate.getCurrentEpNo());
                    drama.setEstRuntimePerEp(dramaToUpdate.getEstRuntimePerEp());
                    drama.setKrAgeRestriction(dramaToUpdate.getKrAgeRestriction());
                    drama.setReleaseYear(dramaToUpdate.getReleaseYear());
                    drama.setStatus(dramaToUpdate.getStatus());
                    drama.setKrReleaseSchedule(dramaToUpdate.getKrReleaseSchedule());
                    drama.setGenres(dramaToUpdate.getGenres());
                    drama.setNetworks(dramaToUpdate.getNetworks());
                    drama.setDramaTwPlatformMap(dramaToUpdate.getDramaTwPlatformMap());
                    drama.setLeadActors(dramaToUpdate.getLeadActors());
                    drama.setDirectorNames(dramaToUpdate.getDirectorNames());
                    drama.setScriptwriterNames(dramaToUpdate.getScriptwriterNames());
                    drama.setMainPosterUrl(dramaToUpdate.getMainPosterUrl());
                    if (!apiMode) {
                        drama.setTrailerUrl(dramaToUpdate.getTrailerUrl());
                    }
                    drama.setIntroPageUrl(dramaToUpdate.getIntroPageUrl());
                    if (!apiMode) {
                        drama.setChineseWikipediaPageUrl(dramaToUpdate.getChineseWikipediaPageUrl());
                    }
                    if (!apiMode) {
                        drama.setNamuWikiPageUrl(dramaToUpdate.getNamuWikiPageUrl());
                    }
                    if (drama.isAiOrManuallyEdited() == false) {
                        drama.setAiOrManuallyEdited(dramaToUpdate.isAiOrManuallyEdited());
                    }
                    drama.setLastUpdatedByApi(dramaToUpdate.getLastUpdatedByApi());
                    return dramaRepository.save(drama);
                })
                .orElseGet(() -> {
                    dramaToUpdate.setDramaId(id);
                    return dramaRepository.save(dramaToUpdate);
                });
    }

    // D: Delete a drama
    @Transactional
    public void deleteDrama(@PathVariable Integer id) {
        // First, remove relationships with existing actors who act in the drama to delete
        Drama dramaToDelete = getDramaById(id).get();
        List<Actor> actorsInDrama = actorRepository.findByDramasContaining(dramaToDelete);

        for (Actor actor : actorsInDrama) {
            actor.removeDramaFromActor(dramaToDelete);
        }

        // Then, safely delete the drama
        dramaRepository.deleteById(id);
    }

    // D-Bonus: Check if a drama exists in database before deletion
    public boolean dramaExists(@PathVariable Integer id) {
        return dramaRepository.existsById(id);
    }
}

