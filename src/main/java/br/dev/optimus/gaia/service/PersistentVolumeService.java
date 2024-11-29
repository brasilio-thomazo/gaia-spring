package br.dev.optimus.gaia.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import br.dev.optimus.gaia.AccessMode;
import br.dev.optimus.gaia.exception.BadRequestException;
import br.dev.optimus.gaia.exception.NotFoundException;
import br.dev.optimus.gaia.model.DBPersistentVolume;
import br.dev.optimus.gaia.repository.PersistentVolumeRepository;
import br.dev.optimus.gaia.request.PersistentVolumeCreateRequest;
import br.dev.optimus.gaia.request.PersistentVolumeUpdateRequest;
import br.dev.optimus.gaia.response.PersistentVolumeResponse;
import io.fabric8.kubernetes.api.model.HostPathVolumeSource;
import io.fabric8.kubernetes.api.model.NFSVolumeSource;
import io.fabric8.kubernetes.api.model.PersistentVolume;
import io.fabric8.kubernetes.api.model.PersistentVolumeBuilder;
import io.fabric8.kubernetes.api.model.PersistentVolumeList;
import io.fabric8.kubernetes.api.model.PersistentVolumeSpecBuilder;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.client.KubernetesClient;

@Service
public class PersistentVolumeService {
    private final KubernetesClient client;
    private final PersistentVolumeRepository repository;
    private final Logger log = LoggerFactory.getLogger(PersistentVolumeService.class);

    public PersistentVolumeService(KubernetesClient client, PersistentVolumeRepository repository) {
        this.client = client;
        this.repository = repository;
    }

    private PersistentVolume pv(DBPersistentVolume data) {
        var spec = new PersistentVolumeSpecBuilder()
                .withCapacity(Map.of("storage", Quantity.parse(data.getCapacity())))
                .withAccessModes(data.getAccessMode().name());

        if (data.getType().equals("nfs")) {
            spec.withNewNfs()
                    .withServer(data.getConfigs().get("server"))
                    .withPath(data.getConfigs().get("path"))
                    .endNfs();
        }

        if (data.getType().equals("hostPath")) {
            spec.withNewHostPath()
                    .withPath(data.getConfigs().get("path"))
                    .endHostPath();
        }

        return new PersistentVolumeBuilder()
                .withNewMetadata()
                .withName(data.getName())
                .withNamespace(data.getNamespace())
                .endMetadata()
                .withSpec(spec.build())
                .build();
    }

    private PersistentVolume pv(String namespace, String name) {
        return new PersistentVolumeBuilder()
                .withNewMetadata()
                .withName(name)
                .withNamespace(namespace)
                .endMetadata()
                .build();
    }

    private void validate(DBPersistentVolume data) {
        if (data.getName() == null || data.getName().isEmpty()) {
            throw new BadRequestException("persistent volume name is required", "name");
        }

        if (data.getCapacity() == null || data.getCapacity().isEmpty()) {
            throw new BadRequestException("persistent volume capacity is required", "capacity");
        }

        if (data.getAccessMode() == null) {
            throw new BadRequestException("persistent volume access mode is required", "access_mode");
        }

        if (data.getType() == null || data.getType().isEmpty()) {
            throw new BadRequestException("persistent volume type is required", "type");
        }

        if (data.getConfigs() == null || data.getConfigs().isEmpty()) {
            throw new BadRequestException("persistent volume configs is required", "configs");
        }

        if (data.getType().equals("nfs")) {
            validateNFS(data.getConfigs());
        }
    }

    private void validateNFS(HashMap<String, String> configs) {
        if (configs.get("server") == null || configs.get("server").isEmpty()) {
            throw new BadRequestException("persistent volume nfs server is required", "server");
        }

        if (configs.get("path") == null || configs.get("path").isEmpty()) {
            throw new BadRequestException("persistent volume nfs path is required", "path");
        }
    }

    public PersistentVolumeList list() {
        return client.persistentVolumes().list();
    }

    public List<DBPersistentVolume> getAll() {
        return repository.findByDeletedAtIsNull();
    }

    public DBPersistentVolume get(long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("persistent volume not found"));
    }

    public PersistentVolumeResponse get(String namespace, String name) {
        log.info("get persistent volume: {}", namespace + "/" + name);
        var pv = client.persistentVolumes().resource(pv(namespace, name)).get();
        var data = repository.findByNamespaceAndName(namespace, name)
                .orElseThrow(() -> new NotFoundException("persistent volume not found"));
        return new PersistentVolumeResponse(data, pv);
    }

    public DBPersistentVolume create(DBPersistentVolume data) {
        var now = Instant.now().getEpochSecond();
        if (repository.existsByNamespaceAndName(data.getNamespace(), data.getName())) {
            throw new BadRequestException("persistent volume name already exists", "name");
        }
        validate(data);
        client.persistentVolumes().resource(pv(data)).create();
        data.setCreatedAt(now);
        data.setUpdatedAt(now);
        return repository.save(data);
    }

    public DBPersistentVolume create(PersistentVolumeCreateRequest request) {
        var data = DBPersistentVolume.builder()
                .namespace(request.namespace())
                .name(request.name())
                .capacity(request.capacity())
                .accessMode(request.accessMode())
                .type(request.type())
                .configs(request.configs())
                .build();

        return create(data);
    }

    public DBPersistentVolume update(DBPersistentVolume data) {
        validate(data);
        client.persistentVolumes().resource(pv(data)).update();
        data.setUpdatedAt(Instant.now().getEpochSecond());
        return repository.save(data);
    }

    public DBPersistentVolume update(long id, PersistentVolumeUpdateRequest request) {
        var data = repository.findById(id).orElseThrow(() -> new NotFoundException("persistent volume not found"));
        data.setCapacity(request.capacity());
        data.setAccessMode(request.accessMode());
        data.setType(request.type());
        data.setConfigs(request.configs());
        return update(data);
    }

    public void delete(Long id) {
        var data = repository.findById(id).orElseThrow(() -> new NotFoundException("persistent volume not found"));
        client.persistentVolumes().resource(pv(data)).delete();
        data.setDeletedAt(Instant.now().getEpochSecond());
        repository.save(data);
    }

    private HashMap<String, String> getConfigs(NFSVolumeSource source) {
        var configs = new HashMap<String, String>();
        configs.put("server", source.getServer());
        configs.put("path", source.getPath());
        return configs;
    }

    private HashMap<String, String> getConfigs(HostPathVolumeSource source) {
        var configs = new HashMap<String, String>();
        configs.put("path", source.getPath());
        return configs;
    }

    public List<DBPersistentVolume> sync() {
        log.info("syncing persistent volumes");
        var list = new ArrayList<DBPersistentVolume>();
        for (var pv : client.persistentVolumes().list().getItems()) {
            var now = Instant.now().getEpochSecond();
            var namespace = pv.getMetadata().getNamespace();
            var name = pv.getMetadata().getName();
            var capacity = pv.getSpec().getCapacity().get("storage").toString();
            var accessMode = AccessMode.valueOf(pv.getSpec().getAccessModes().get(0));
            var type = pv.getSpec().getNfs() != null ? "nfs" : "hostPath";
            var configs = type == "nfs" ? getConfigs(pv.getSpec().getNfs()) : getConfigs(pv.getSpec().getHostPath());
            if (!repository.existsByNamespaceAndName(pv.getMetadata().getNamespace(), pv.getMetadata().getName())) {
                var data = DBPersistentVolume.builder()
                        .namespace(namespace)
                        .name(name)
                        .capacity(capacity)
                        .accessMode(accessMode)
                        .type(type)
                        .configs(configs)
                        .build();
                data.setCreatedAt(now);
                data.setUpdatedAt(now);
                list.add(data);
                continue;
            }

            var data = repository.findByNamespaceAndName(namespace, name)
                    .orElseThrow(() -> new NotFoundException("persistent volume not found"));
            data.setCapacity(capacity);
            data.setAccessMode(accessMode);
            data.setType(type);
            data.setConfigs(configs);
            data.setUpdatedAt(now);
            list.add(data);
        }
        return repository.saveAll(list);
    }
}
