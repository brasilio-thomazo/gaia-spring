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

import br.dev.optimus.gaia.request.PersistentVolumeClaimCreateRequest;
import br.dev.optimus.gaia.request.PersistentVolumeClaimUpdateRequest;
import br.dev.optimus.gaia.service.PersistentVolumeClaimService;

@RestController
@RequestMapping("/pvc")
public class PersistentVolumeClaimController {
    private final PersistentVolumeClaimService service;

    public PersistentVolumeClaimController(PersistentVolumeClaimService service) {
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

    @GetMapping("/{namespace}/{name}")
    public ResponseEntity<?> get(@PathVariable String namespace, @PathVariable String name) {
        return ResponseEntity.ok(service.get(namespace, name));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody PersistentVolumeClaimCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable long id, @RequestBody PersistentVolumeClaimUpdateRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
