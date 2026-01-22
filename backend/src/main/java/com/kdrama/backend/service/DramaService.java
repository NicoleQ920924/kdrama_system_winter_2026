package com.kdrama.backend.service;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

    private final ObjectMapper objectMapper;

    public DramaService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    // Refer to DramaRepository.java for the CRUD repository methods

    // C1-1: Call TMDB API to fetch drama information based on chineseName
    // Multiple dramas if there are multiple seasons
    public ArrayList<Drama> getDramasFromTmdbByChineseName(String title) {
        try {
            Integer tmdbId = tmdbDramaClient.getDramaTmdbIdByChineseName(title);
            ArrayList<Drama> fetchedDramas = getDramasFromTmdbByTmdbId(tmdbId, null);
            return fetchedDramas;
		} catch (Exception e) {
			System.err.println("Exception Occurred!" + e.getMessage());
            return null;
		}
    }

    // C1-2: Call TMDB API to fetch drama information based on tmdbId
    // Multiple dramas if there are multiple seasons
    // searchedActorName is passed by TmdbActorClient.java
    public ArrayList<Drama> getDramasFromTmdbByTmdbId(Integer tmdbId, String searchedActorName) {
        try {
            ArrayList<Drama> fetchedDramas = tmdbDramaClient.fillDramaInfoByTmdbId(tmdbId);
            if (fetchedDramas != null) {
                tmdbDramaClient.fillEngName(fetchedDramas);
                tmdbDramaClient.fillKrAgeRestriction(fetchedDramas);
                tmdbDramaClient.fillEpInfo(fetchedDramas);
                tmdbDramaClient.fillDramaStaff(fetchedDramas, searchedActorName);
                fillTWPlatformInformation(fetchedDramas);
            }
            return fetchedDramas;
		} catch (Exception e) {
			System.err.println("Exception Occurred!" + e.getMessage());
            e.printStackTrace();
            return null;
		}
    }
    
    // C1-3: Fill TW OTT Platform Information (parameter: ArrayList<Drama>)
    public ArrayList<Drama> fillTWPlatformInformation(ArrayList<Drama> fetchedDramas) {
        try {
            if (fetchedDramas.equals(null) || fetchedDramas.isEmpty()) {
                System.err.println("fetchedDramas is null or empty, so platform information cannot be filled!");
                return fetchedDramas;
            }

            System.out.println("No. of dramas to process: " + fetchedDramas.size());

            for (Drama drama : fetchedDramas) {
                try {
                    Map<String, String> platformInfoMap = platformScraper.getWorkTWOTTPlatformInfo(drama.getChineseName(), "drama");
                    drama.setDramaTwPlatformMap(platformInfoMap);
                    platformInfoMap.putAll(tmdbPlatformClient.getIntlPlatformInfoByWorkTmdbId(drama.getTmdbId(), drama.getSeasonNumber(), "drama"));
                    System.out.println(drama.getChineseName() + ": " + platformInfoMap.keySet());
                    drama.setLastUpdatedByApi(LocalDateTime.now());
                } catch (Exception innerE) {
                    System.err.println("Failed to save platform information of " + drama.getChineseName() + " due to" + innerE.getMessage());
                    innerE.printStackTrace();
                }
            }

            String backupFilePath = "backup/drama_backup.json";
            File backupFile = new File(backupFilePath);
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(backupFile, fetchedDramas);
            System.out.println("Successfully save file: " + backupFile);

            return fetchedDramas;

        } catch (Exception e) {
            System.err.println("Exception Occurred!" + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // C1-4: Call TMDB API to fetch drama information based on tmdbId AND season number
    // Called by DramaController.java's updateSelectedDramaViaApi()
    public Drama getDramaFromTmdbByTmdbIdAndSeasonNumber(Integer tmdbId, Integer seasonNumber) {
        try {
            Drama fetchedDrama = tmdbDramaClient.fillDramaInfoByTmdbIdAndSeasonNumber(tmdbId, seasonNumber);
            if (fetchedDrama != null) {
                tmdbDramaClient.fillEngName(fetchedDrama);
                tmdbDramaClient.fillKrAgeRestriction(fetchedDrama);
                tmdbDramaClient.fillEpInfo(fetchedDrama, isMultipleSeasons(fetchedDrama));
                tmdbDramaClient.fillDramaStaff(fetchedDrama);
                fillTWPlatformInformation(fetchedDrama);
            }
            return fetchedDrama;
		} catch (Exception e) {
			System.err.println("Exception Occurred!" + e.getMessage());
            e.printStackTrace();
            return null;
		}
    }

    // C1-5: Fill TW OTT Platform Information (parameter: a single Drama object only)
    public Drama fillTWPlatformInformation(Drama fetchedDrama) {
        try {
            if (fetchedDrama.equals(null)) {
                System.err.println("fetchedDrama is null or empty, so platform information cannot be filled!");
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
   
    // C2-1: Save a drama
    public Drama saveDrama(@RequestBody Drama drama) {
        return dramaRepository.save(drama);
    }

    // C2-2: Save multiple seasons of a drama
    public List<Drama> saveDramaAllSeasons (@RequestBody List<Drama> dramaSeasons) {
        if (dramaSeasons != null) {
            return dramaRepository.saveAll(dramaSeasons);
        }
        else {
            return null;
        }
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

    // R4: Get information of dramas identified by Chinese title
    public Optional<List<Drama>> getDramasByChineseName(@PathVariable String chineseName) {
        return dramaRepository.findByChineseName(chineseName);
    }

    // U1: Update a drama
    public Drama updateDrama(@PathVariable Integer id, @RequestBody Drama updatedDrama, boolean apiMode) {
        return dramaRepository.findById(id)
                .map(drama -> {
                    drama.setTmdbId(updatedDrama.getTmdbId());
                    drama.setSeasonNumber(updatedDrama.getSeasonNumber());
                    if ((drama.getChineseName().isEmpty() && apiMode) || !apiMode) {
                        drama.setChineseName(updatedDrama.getChineseName());
                    }
                    if ((drama.getEnglishName().isEmpty() && apiMode) || !apiMode) {
                        drama.setEnglishName(updatedDrama.getEnglishName());
                    }
                    if ((drama.getKoreanName().isEmpty() && apiMode) || !apiMode) {
                        drama.setKoreanName(updatedDrama.getKoreanName());
                    }
                    drama.setTotalNumOfEps(updatedDrama.getTotalNumOfEps());
                    drama.setCurrentEpNo(updatedDrama.getCurrentEpNo());
                    drama.setEstRuntimePerEp(updatedDrama.getEstRuntimePerEp());
                    drama.setKrAgeRestriction(updatedDrama.getKrAgeRestriction());
                    drama.setReleaseYear(updatedDrama.getReleaseYear());
                    drama.setStatus(updatedDrama.getStatus());
                    drama.setKrReleaseSchedule(updatedDrama.getKrReleaseSchedule());
                    drama.setGenres(updatedDrama.getGenres());
                    drama.setNetworks(updatedDrama.getNetworks());
                    drama.setDramaTwPlatformMap(updatedDrama.getDramaTwPlatformMap());
                    drama.setLeadActors(updatedDrama.getLeadActors());
                    drama.setDirectorNames(updatedDrama.getDirectorNames());
                    drama.setScriptwriterNames(updatedDrama.getScriptwriterNames());
                    drama.setMainPosterUrl(updatedDrama.getMainPosterUrl());
                    if (!apiMode) {
                        drama.setTrailerUrl(updatedDrama.getTrailerUrl());
                    }
                    drama.setIntroPageUrl(updatedDrama.getIntroPageUrl());
                    if (!apiMode) {
                        drama.setNamuWikiPageUrl(updatedDrama.getNamuWikiPageUrl());
                    }
                    if (drama.isManuallyEdited() == false) {
                        drama.setManuallyEdited(updatedDrama.isManuallyEdited());
                    }
                    drama.setLastUpdatedByApi(updatedDrama.getLastUpdatedByApi());
                    return dramaRepository.save(drama);
                })
                .orElseGet(() -> {
                    updatedDrama.setDramaId(id);
                    return dramaRepository.save(updatedDrama);
                });
    }

    // U2: Update multiple seasons of a drama
    // newDramaSeasons > originalDramaSeasons: Update if the season exists, save if the season does not exist
    // originalDramaSeasons > newDramaSeasons (due to some seasons are filtered while searching for an actor): Update existing seasons only 
    public List<Drama> updateDramaAllSeasons(List<Drama> originalDramaSeasons, List<Drama> newDramaSeasons) {
        int originalSeasonCount = originalDramaSeasons.size();
        int newSeasonCount = newDramaSeasons.size();
        ArrayList<Drama> updatedDramas = new ArrayList<Drama>();

        if (newSeasonCount > originalSeasonCount) {
            for (int i = 0; i < originalSeasonCount; i++) {
                if (originalDramaSeasons.get(i).getSeasonNumber() == newDramaSeasons.get(i).getSeasonNumber()) {
                    updatedDramas.add(updateDrama(originalDramaSeasons.get(i).getDramaId(), newDramaSeasons.get(i), true));
                }
            }
            for (int i = originalSeasonCount; i < newSeasonCount; i++) {
                updatedDramas.add(saveDrama(newDramaSeasons.get(i)));
            }
        }
        else {
            for (int i = 0; i < newSeasonCount; i++) {
                if (originalDramaSeasons.get(i).getSeasonNumber() == newDramaSeasons.get(i).getSeasonNumber()) {
                    updatedDramas.add(updateDrama(originalDramaSeasons.get(i).getDramaId(), newDramaSeasons.get(i), true));
                }
            }
        }

        return updatedDramas;
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

