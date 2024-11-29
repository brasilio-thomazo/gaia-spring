package br.dev.optimus.gaia.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.dev.optimus.gaia.request.PersistentVolumeCreateRequest;
import br.dev.optimus.gaia.request.PersistentVolumeUpdateRequest;
import br.dev.optimus.gaia.service.PersistentVolumeService;

@RestController
@RequestMapping("/pv")
public class PersistentVolumeController {
    private final PersistentVolumeService service;

    public PersistentVolumeController(PersistentVolumeService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<?> index() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> show(@PathVariable long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody PersistentVolumeCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable long id, @RequestBody PersistentVolumeUpdateRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{namespace}/{name}")
    public ResponseEntity<?> get(@PathVariable String namespace, @PathVariable String name) {
        return ResponseEntity.ok(service.get(namespace, name));
    }
}