package br.dev.optimus.gaia.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import br.dev.optimus.gaia.AccessMode;
import br.dev.optimus.gaia.exception.BadRequestException;
import br.dev.optimus.gaia.exception.NotFoundException;
import br.dev.optimus.gaia.model.DBPersistentVolumeClaim;
import br.dev.optimus.gaia.repository.PersistentVolumeClaimRepository;
import br.dev.optimus.gaia.request.PersistentVolumeClaimCreateRequest;
import br.dev.optimus.gaia.request.PersistentVolumeClaimUpdateRequest;
import br.dev.optimus.gaia.response.PersistentVolumeClaimResponse;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaimBuilder;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaimSpecBuilder;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.client.KubernetesClient;

@Service
public class PersistentVolumeClaimService {
    private final KubernetesClient client;
    private final PersistentVolumeClaimRepository repository;
    private final Environment env;
    private final Logger log = LoggerFactory.getLogger(PersistentVolumeClaimService.class);
    private final String namespace;

    public PersistentVolumeClaimService(KubernetesClient client, PersistentVolumeClaimRepository repository,
            Environment env) {
        this.client = client;
        this.repository = repository;
        this.env = env;
        log.info("default namespace: {}", env.getProperty("kubernetes.namespace"));
        this.namespace = env.getProperty("kubernetes.namespace", "default");
    }

    private void validate(DBPersistentVolumeClaim data) {
        if (data.getName() == null || data.getName().isEmpty()) {
            throw new BadRequestException("persistent volume claim name is required", "name");
        }

        if (data.getAccessMode() == null) {
            throw new BadRequestException("persistent volume claim access mode is required", "access_mode");
        }

        if (data.getCapacity() == null || data.getCapacity().isEmpty()) {
            throw new BadRequestException("persistent volume claim capacity is required", "capacity");
        }

        if (data.getVolumeName() == null || data.getVolumeName().isEmpty()) {
            throw new BadRequestException("persistent volume claim volume name is required", "volume_name");
        }
    }

    private PersistentVolumeClaim pvc(DBPersistentVolumeClaim data) {
        var spec = new PersistentVolumeClaimSpecBuilder()
                .withAccessModes(data.getAccessMode().name())
                .withVolumeName(data.getVolumeName())
                .withNewResources()
                .withRequests(Map.of("storage", Quantity.parse(data.getCapacity())))
                .endResources()
                .build();

        return new PersistentVolumeClaimBuilder()
                .withNewMetadata()
                .withName(data.getName())
                .withNamespace(data.getNamespace())
                .endMetadata()
                .withSpec(spec)
                .build();
    }

    private PersistentVolumeClaim pvc(String namespace, String name) {
        return new PersistentVolumeClaimBuilder()
                .withNewMetadata()
                .withName(name)
                .withNamespace(namespace)
                .endMetadata()
                .build();
    }

    public List<DBPersistentVolumeClaim> getAll() {
        return repository.findByDeletedAtIsNull();
    }

    public DBPersistentVolumeClaim get(long id) {
        log.info("get persistent volume claim: {}", id);
        return repository.findById(id).orElseThrow(() -> new NotFoundException("persistent volume claim not found"));
    }

    public PersistentVolumeClaimResponse get(String namespace, String name) {
        log.info("get persistent volume claim: {}", namespace + "/" + name);
        var pvc = client.persistentVolumeClaims().resource(pvc(namespace, name)).get();
        var data = repository.findByNamespaceAndName(namespace, name)
                .orElseThrow(() -> new NotFoundException("persistent volume claim not found"));
        return new PersistentVolumeClaimResponse(pvc, data);
    }

    public DBPersistentVolumeClaim create(DBPersistentVolumeClaim data) {
        var now = Instant.now().getEpochSecond();
        log.info("creating persistent volume claim: {}", data);
        if (repository.existsByNamespaceAndName(data.getNamespace(), data.getName())) {
            throw new BadRequestException("persistent volume claim name already exists", "name");
        }
        validate(data);
        client.persistentVolumeClaims().resource(pvc(data)).create();
        data.setCreatedAt(now);
        data.setUpdatedAt(now);
        return repository.save(data);
    }

    public DBPersistentVolumeClaim create(PersistentVolumeClaimCreateRequest request) {
        var data = DBPersistentVolumeClaim.builder()
                .name(request.name())
                .namespace(request.namespace())
                .capacity(request.capacity())
                .accessMode(request.accessMode())
                .volumeName(request.volumeName())
                .build();
        return create(data);
    }

    public DBPersistentVolumeClaim update(DBPersistentVolumeClaim data) {
        log.info("updating persistent volume claim: {}", data);
        validate(data);
        client.persistentVolumeClaims().resource(pvc(data)).update();
        data.setUpdatedAt(Instant.now().getEpochSecond());
        return repository.save(data);
    }

    public DBPersistentVolumeClaim update(long id, PersistentVolumeClaimUpdateRequest request) {
        var data = get(id);
        data.setCapacity(request.capacity());
        data.setAccessMode(request.accessMode());
        data.setVolumeName(request.volumeName());
        return update(data);
    }

    public void delete(Long id) {
        log.info("deleting persistent volume claim: {}", id);
        var data = get(id);
        client.persistentVolumeClaims().resource(pvc(data)).delete();
        data.setDeletedAt(Instant.now().getEpochSecond());
        repository.save(data);
    }

    public List<DBPersistentVolumeClaim> sync() {
        log.info("syncing persistent volume claims");
        var list = new ArrayList<DBPersistentVolumeClaim>();
        for (var pvc : client.persistentVolumeClaims().inNamespace(namespace).list().getItems()) {
            var now = Instant.now().getEpochSecond();
            var namespace = pvc.getMetadata().getNamespace();
            var name = pvc.getMetadata().getName();
            var capacity = pvc.getSpec().getResources().getRequests().get("storage").toString();
            var accessMode = AccessMode.valueOf(pvc.getSpec().getAccessModes().get(0));
            var volumeName = pvc.getSpec().getVolumeName();
            if (!repository.existsByNamespaceAndName(pvc.getMetadata().getNamespace(), pvc.getMetadata().getName())) {
                var data = DBPersistentVolumeClaim.builder()
                        .namespace(namespace)
                        .name(name)
                        .capacity(capacity)
                        .accessMode(accessMode)
                        .volumeName(volumeName)
                        .build();
                data.setCreatedAt(now);
                data.setUpdatedAt(now);
                list.add(data);
                continue;
            }

            if (List.of(env.getActiveProfiles()).contains("dev")) {
                var data = repository.findByNamespaceAndName(namespace, name)
                        .orElseThrow(() -> new NotFoundException("persistent volume claim not found"));
                data.setCapacity(capacity);
                data.setAccessMode(accessMode);
                data.setVolumeName(volumeName);
                data.setUpdatedAt(now);
                list.add(data);
            }
        }
        return repository.saveAll(list);
    }
}
