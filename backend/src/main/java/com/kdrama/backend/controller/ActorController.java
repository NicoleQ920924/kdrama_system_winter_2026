package com.kdrama.backend.controller;

import java.util.List;
import java.util.Optional;

import org.apache.hc.core5.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.kdrama.backend.model.Actor;
import com.kdrama.backend.service.ActorService;
import com.kdrama.backend.service.AiService;
import com.kdrama.backend.util.DisplayNameEnumSerializer;

@RestController
@RequestMapping("/api/actors")
public class ActorController {
    @Autowired
    private ActorService actorService;

    private final ObjectMapper objectMapper;

    private final AiService aiService;

    public ActorController(ObjectMapper objectMapper, AiService aiService) {
        this.objectMapper = objectMapper;
        this.aiService = aiService;
    }

    @PostMapping("/import")
    @com.kdrama.backend.security.RequireRole({com.kdrama.backend.enums.Role.ADMIN})
    public ResponseEntity<?> importActor(@RequestParam String name) {
        // Check if the actor already exists in database
        Optional<Actor> optionalExistingActor = actorService.getActorByChineseName(name);
        if (optionalExistingActor.isPresent()) {
            // actor 已存在 → 回傳 409
            return ResponseEntity.status(HttpStatus.SC_CONFLICT)
                                .body("Actor already exists in database");
        }
        Actor actor = actorService.fillActorBasicInfo(name);
        if (actor == null) {
            return ResponseEntity.notFound().build();
        }
        // Check if the actor already exists in database
        optionalExistingActor = actorService.getActorByTmdbId(actor.getTmdbId());
        if (optionalExistingActor.isPresent()) {
            // actor 已存在 → 回傳 409
            return ResponseEntity.status(HttpStatus.SC_CONFLICT)
                                .body("Actor already exists in database");
        }
        
        actor = actorService.fillActorMoreInfo(actor, true);

        if (actor == null) {
            return ResponseEntity.notFound().build();
        }
        else {
            actor = aiService.aiUpdateActorInfo(actor);
            if (actor == null) {
                return ResponseEntity.notFound().build();
            }
            else {
                Actor savedActor = actorService.saveActor(actor);
                return ResponseEntity.ok(savedActor);
            }    
        }
    }

    @GetMapping("/findAll")
    public ResponseEntity<JsonNode> findActors(
            @RequestParam(required = false, defaultValue = "true") boolean displayNameMode) {

        List<Actor> actors = actorService.getAllActors();
        if (actors.isEmpty()) {
            return ResponseEntity.noContent().build(); // 204
        }

        SimpleModule module = new SimpleModule();
        module.addSerializer(Enum.class, new DisplayNameEnumSerializer(displayNameMode));

        ObjectMapper mapper = objectMapper.copy();
        mapper.registerModule(module);

        try {
            JsonNode jsonNode = mapper.valueToTree(actors);
            return ResponseEntity.ok(jsonNode);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<JsonNode> findSelectedActor(
            @PathVariable Integer id,
            @RequestParam(required = false, defaultValue = "true") boolean displayNameMode) {

        Optional<Actor> optionalActor = actorService.getActorById(id);
        if (optionalActor.isEmpty()) {
            return ResponseEntity.noContent().build(); // 204
        }

        Actor actor = optionalActor.get();

        SimpleModule module = new SimpleModule();
        module.addSerializer(Enum.class, new DisplayNameEnumSerializer(displayNameMode));

        ObjectMapper mapper = objectMapper.copy();
        mapper.registerModule(module);

        try {
            // Return JsonNode to frontend
            JsonNode jsonNode = mapper.valueToTree(actor);
            return ResponseEntity.ok(jsonNode);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/chineseName")
    public ResponseEntity<JsonNode> findSelectedActorByChineseName(
        @RequestParam String chineseName,
        @RequestParam(required = false, defaultValue = "true") boolean displayNameMode) {
        
        Optional<Actor> optionalActor = actorService.getActorByChineseName(chineseName);
        if (optionalActor.isEmpty()) {
            return ResponseEntity.noContent().build(); // 204
        }
        
        Actor actor = optionalActor.get();

        SimpleModule module = new SimpleModule();
        module.addSerializer(Enum.class, new DisplayNameEnumSerializer(displayNameMode));

        ObjectMapper mapper = objectMapper.copy();
        mapper.registerModule(module);

        try {
            // Return JsonNode to frontend
            JsonNode jsonNode = mapper.valueToTree(actor);
            return ResponseEntity.ok(jsonNode);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/apiupdate/{id}")
    @com.kdrama.backend.security.RequireRole({com.kdrama.backend.enums.Role.ADMIN})
    public ResponseEntity<Actor> updateSelectedActorViaApi(@PathVariable Integer id,
        @RequestParam boolean includesExistingWork) {
        
        Optional<Actor> optionalActor = actorService.getActorById(id);
        if (optionalActor.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        else {
            Actor actor = optionalActor.get();
            Actor updatedActor = actorService.fillActorMoreInfo(actor, includesExistingWork);
            if (updatedActor == null) {
                return ResponseEntity.notFound().build();
            }
            else {
                Actor savedActor = actorService.updateActor(id, updatedActor, true);
                return ResponseEntity.ok(savedActor);
            } 
        }
    }

    @PutMapping("/aiupdate/{id}")
    @com.kdrama.backend.security.RequireRole({com.kdrama.backend.enums.Role.ADMIN})
    public ResponseEntity<?> updateSelectedActorViaAiAndForm(@PathVariable Integer id, @RequestBody Actor actorToUpdate) {
       Actor aiUpdated = aiService.aiUpdateActorInfo(actorToUpdate);
       Actor actor = actorService.updateActor(id, aiUpdated, false);
       return ResponseEntity.ok(actor);
    }

    @PutMapping("/formupdate/{id}")
    @com.kdrama.backend.security.RequireRole({com.kdrama.backend.enums.Role.ADMIN})
    public ResponseEntity<Actor> updateSelectedActorViaForm(@PathVariable Integer id, @RequestBody Actor actorToUpdate) {
        Actor actor = actorService.updateActor(id, actorToUpdate, false);
        return ResponseEntity.ok(actor);
    }

    @DeleteMapping("/delete/{id}")
    @com.kdrama.backend.security.RequireRole({com.kdrama.backend.enums.Role.ADMIN})
    public ResponseEntity<String> deleteSelectedActor(@PathVariable Integer id) {     
        if (actorService.actorExists(id)) {
            actorService.deleteActor(id);
            return ResponseEntity.ok("Actor deleted successfully");
        } else {
            return ResponseEntity.status(HttpStatus.SC_NOT_FOUND).body("Actor not found");
        }
    }
}
