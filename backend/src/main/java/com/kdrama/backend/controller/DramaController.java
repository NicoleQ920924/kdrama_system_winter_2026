package com.kdrama.backend.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.kdrama.backend.model.Drama;
import com.kdrama.backend.service.AiService;
import com.kdrama.backend.service.DramaService;
import com.kdrama.backend.util.DisplayNameEnumSerializer;

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
    
    private AiService aiService;

    public DramaController(ObjectMapper objectMapper, AiService aiService) {
        this.objectMapper = objectMapper;
        this.aiService = aiService;
    }

    @PostMapping("/import")
    public ResponseEntity<?> importDrama(@RequestParam String name) {
        
        // Check if the drama already exists in database
        Optional<Drama> optionalExistingDrama = dramaService.getDramaByChineseName(name);
        if (optionalExistingDrama.isPresent()) {
            // drama 已存在 → 回傳 409
            return ResponseEntity.status(HttpStatus.SC_CONFLICT)
                                .body("Drama already exists in database");
        }
        
        Drama drama = dramaService.fillDramaBasicInfo(name);
        if (drama == null) {
            return ResponseEntity.notFound().build(); // 404
        }

        // Check if the drama already exists in database
        optionalExistingDrama = dramaService.getDramaByTmdbIdAndSeasonNumber(drama.getTmdbId(), drama.getSeasonNumber());
        if (optionalExistingDrama.isPresent()) {
            // drama 已存在 → 回傳 409
            return ResponseEntity.status(HttpStatus.SC_CONFLICT)
                                .body("Drama already exists in database");
        }
        
        drama = dramaService.fillDramaMoreInfo(drama, null);

        if (drama == null) {
            return ResponseEntity.notFound().build();
        }
        else {
            drama = aiService.aiUpdateDramaInfo(drama);
            if (drama == null) {
                return ResponseEntity.notFound().build();
            }
            else {
                Drama savedDrama = dramaService.saveDrama(drama);
                return ResponseEntity.ok(savedDrama);
            }    
        }
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
    public ResponseEntity<JsonNode> findSelectedDramaById(
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

    @GetMapping("/chineseName")
    public ResponseEntity<JsonNode> findSelectedDramaByChineseName(
        @RequestParam String chineseName,
        @RequestParam(required = false, defaultValue = "true") boolean displayNameMode) {
        
        Optional<Drama> optionalDrama = dramaService.getDramaByChineseName(chineseName);
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
            Drama updatedDrama = dramaService.fillDramaMoreInfo(drama, null);
            if (updatedDrama == null) {
                return ResponseEntity.notFound().build();
            }
            else {
                Drama savedDrama = dramaService.updateDrama(id, updatedDrama, true);
                return ResponseEntity.ok(savedDrama);
            } 
        }
    }

    @PutMapping("/aiupdate/{id}")
    public ResponseEntity<?> updateSelectedDramaViaAiAndForm(@PathVariable Integer id, @RequestBody Drama dramaToUpdate) {
       Drama aiUpdated = aiService.aiUpdateDramaInfo(dramaToUpdate);
       Drama drama = dramaService.updateDrama(id, aiUpdated, false);
       return ResponseEntity.ok(drama);
    }

    @PutMapping("/allupdate/{id}")
    public ResponseEntity<Drama> updateSelectedDramaViaForm(@PathVariable Integer id, @RequestBody Drama dramaToUpdate) {
        Drama drama = dramaService.updateDrama(id, dramaToUpdate, false);
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
