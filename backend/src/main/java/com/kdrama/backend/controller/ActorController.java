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
import com.kdrama.backend.util.DisplayNameEnumSerializer;

@RestController
@RequestMapping("/api/actors")
public class ActorController {
    @Autowired
    private ActorService actorService;

    private final ObjectMapper objectMapper;

    public ActorController(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostMapping("/import")
    public ResponseEntity<?> importActor(@RequestParam String name) {
        // Check if the actor already exists in database
        Optional<Actor> existingActor = actorService.getActorByChineseName(name);
        if (existingActor.isPresent()) {
            // actor 已存在 → 回傳 409
            return ResponseEntity.status(HttpStatus.SC_CONFLICT)
                                .body("Actor already exists in database");
        }

        Actor actor = actorService.getActorFromTmdbByChineseName(name);
        if (actor == null) {
            return ResponseEntity.notFound().build();
        }
        Actor saved = actorService.saveActor(actor);
        return ResponseEntity.ok(saved);
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


    @PutMapping("/apiupdate/{id}")
    public ResponseEntity<Actor> updateSelectedActorViaApi(@PathVariable Integer id,
            @RequestParam boolean includesExistingWork) {
        
        Optional<Actor> optionalActor = actorService.getActorById(id);
        if (optionalActor.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        else {
            Actor actor = optionalActor.get();
            Actor updatedActor = actorService.getActorFromTmdbByTmdbId(actor.getTmdbId(), includesExistingWork);
            Actor savedActor = actorService.updateActor(id, updatedActor, true);
            return ResponseEntity.ok(savedActor);
        }
    }

    @PutMapping("/allupdate/{id}")
    public ResponseEntity<Actor> updateSelectedActorAllInfo(@PathVariable Integer id, @RequestBody Actor updatedActor) {
        Actor actor = actorService.updateActor(id, updatedActor, false);
        return ResponseEntity.ok(actor);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteSelectedActor(@PathVariable Integer id) {     
        if (actorService.actorExists(id)) {
            actorService.deleteActor(id);
            return ResponseEntity.ok("Actor deleted successfully");
        } else {
            return ResponseEntity.status(HttpStatus.SC_NOT_FOUND).body("Actor not found");
        }
    }
}
