package com.kdrama.backend.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.kdrama.backend.model.Drama;
import com.kdrama.backend.service.DramaService;
import com.kdrama.backend.util.DisplayNameEnumSerializer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.hc.core5.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dramas")
public class DramaController {

    @Autowired
    private DramaService dramaService;

    private final ObjectMapper objectMapper;

    public DramaController(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostMapping("/import")
    public ResponseEntity<?> importDrama(@RequestParam String name) {
        // Check if the drama (with one or more seasons) already exists in database
        Optional<List<Drama>> optionalExistingDramas = dramaService.getDramasByChineseName(name);
        List<Drama> existingDramas = optionalExistingDramas.get();
        if (existingDramas.size() > 0) {
            // drama 已存在 → 回傳 409
            return ResponseEntity.status(HttpStatus.SC_CONFLICT)
                                .body("Drama already exists in database");
        }
        ArrayList<Drama> dramas = dramaService.getDramasFromTmdbByChineseName(name);
        if (dramas == null) {
            return ResponseEntity.notFound().build();
        }
        List<Drama> savedDramas = dramaService.saveDramaAllSeasons(dramas);
        return ResponseEntity.ok(savedDramas);
    }

    @GetMapping("/findAll")
    public ResponseEntity<JsonNode> findDramas(
        @RequestParam(required = false, defaultValue = "true") boolean displayNameMode) {
        
        List<Drama> dramas = dramaService.getAllDramas();
        if (dramas.isEmpty()) {
            return ResponseEntity.noContent().build(); // 204
        }

        SimpleModule module = new SimpleModule();
        module.addSerializer(Enum.class, new DisplayNameEnumSerializer(displayNameMode));

        ObjectMapper mapper = objectMapper.copy();
        mapper.registerModule(module);

        try {
            JsonNode jsonNode = mapper.valueToTree(dramas);
            return ResponseEntity.ok(jsonNode);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<JsonNode> findSelectedDrama(
        @PathVariable Integer id,
        @RequestParam(required = false, defaultValue = "true") boolean displayNameMode) {
        
        Optional<Drama> optionalDrama = dramaService.getDramaById(id);
        if (optionalDrama.isEmpty()) {
            return ResponseEntity.noContent().build(); // 204
        }
        
        Drama drama = optionalDrama.get();

        SimpleModule module = new SimpleModule();
        module.addSerializer(Enum.class, new DisplayNameEnumSerializer(displayNameMode));

        ObjectMapper mapper = objectMapper.copy();
        mapper.registerModule(module);

        try {
            // Return JsonNode to frontend
            JsonNode jsonNode = mapper.valueToTree(drama);
            return ResponseEntity.ok(jsonNode);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/apiupdate/{id}")
    public ResponseEntity<?> updateSelectedDramaViaApi(@PathVariable Integer id) {
        
        Optional<Drama> optionalDrama = dramaService.getDramaById(id);
        if (optionalDrama.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        else {
            Drama drama = optionalDrama.get();
            Drama updatedDrama = dramaService.getDramaFromTmdbByTmdbIdAndSeasonNumber(drama.getTmdbId(), drama.getSeasonNumber());
            if (updatedDrama == null) {
                return ResponseEntity.notFound().build();
            }
            else {
                Drama savedDrama = dramaService.updateDrama(id, updatedDrama, true);
                return ResponseEntity.ok(savedDrama);
            } 
        }
    }

    @PutMapping("/allupdate/{id}")
    public ResponseEntity<Drama> updateSelectedDramaAllInfo(@PathVariable Integer id, @RequestBody Drama updatedDrama) {
        Drama drama = dramaService.updateDrama(id, updatedDrama, false);
        return ResponseEntity.ok(drama);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteSelectedDrama(@PathVariable Integer id) {     
        if (dramaService.dramaExists(id)) {
            dramaService.deleteDrama(id);
            return ResponseEntity.ok("Drama deleted successfully");
        } else {
            return ResponseEntity.status(HttpStatus.SC_NOT_FOUND).body("Drama not found");
        }
    }
}
