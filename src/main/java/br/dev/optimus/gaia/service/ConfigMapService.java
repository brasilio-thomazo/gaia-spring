package br.dev.optimus.gaia.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import br.dev.optimus.gaia.exception.BadRequestException;
import br.dev.optimus.gaia.exception.NotFoundException;
import br.dev.optimus.gaia.model.DBConfigMap;
import br.dev.optimus.gaia.repository.ConfigMapRepository;
import br.dev.optimus.gaia.request.ConfigMapRequest;
import io.fabric8.kubernetes.client.KubernetesClient;

@Service
public class ConfigMapService {
    private final KubernetesClient client;
    private final ConfigMapRepository repository;
    private final Environment env;
    private final Logger log = LoggerFactory.getLogger(ConfigMapService.class);
    private final String namespace;

    public ConfigMapService(KubernetesClient client, ConfigMapRepository repository, Environment env) {
        this.client = client;
        this.repository = repository;
        this.env = env;
        this.namespace = env.getProperty("kubernetes.namespace", "default");
    }

    private void validate(DBConfigMap data) {
        if (data.getName() == null || data.getName().isEmpty()) {
            throw new BadRequestException("config map name is required", "name");
        }

        if (data.getData() == null || data.getData().isEmpty()) {
            throw new BadRequestException("config map data is required", "data");
        }

        if (data.getNamespace() == null || data.getNamespace().isEmpty()) {
            data.setNamespace(namespace);
        }
    }

    public List<DBConfigMap> list() {
        return repository.findByDeletedAtIsNull();
    }

    public DBConfigMap get(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("config map not found"));
    }

    public DBConfigMap get(String namespace, String name) {
        return repository.findByNamespaceAndName(namespace, name)
                .orElseThrow(() -> new NotFoundException("config map not found"));
    }

    public DBConfigMap create(DBConfigMap data) {
        var now = Instant.now().getEpochSecond();
        validate(data);
        if (repository.existsByNamespaceAndName(data.getNamespace(), data.getName())) {
            throw new BadRequestException("config map already exists", "name");
        }
        data.setCreatedAt(now);
        data.setUpdatedAt(now);
        client.configMaps().resource(data.configMap()).create();
        log.info("creating config map: {}", data);
        return repository.save(data);
    }

    public DBConfigMap create(ConfigMapRequest.Create request) {
        var data = DBConfigMap.builder()
                .namespace(request.namespace())
                .name(request.name())
                .data(request.data())
                .build();
        return create(data);
    }

    public DBConfigMap update(DBConfigMap data) {
        validate(data);
        data.setUpdatedAt(Instant.now().getEpochSecond());
        client.configMaps().resource(data.configMap()).update();
        log.info("updating config map: {}", data);
        return repository.save(data);
    }

    public DBConfigMap update(Long id, ConfigMapRequest.Update request) {
        var data = get(id);
        data.setData(request.data());
        return update(data);
    }

    public void delete(Long id) {
        log.info("deleting config map: {}", id);
        var data = get(id);
        data.setDeletedAt(Instant.now().getEpochSecond());
        repository.save(data);
    }

    public void sync() {
        log.info("syncing config maps");
        var list = new ArrayList<DBConfigMap>();
        for (var cm : client.configMaps().inNamespace(namespace).list().getItems()) {
            var name = cm.getMetadata().getName();
            var data = cm.getData();
            var now = Instant.now().getEpochSecond();
            if (name.equals("kube-root-ca.crt")) {
                continue;
            }
            if (!repository.existsByNamespaceAndName(namespace, name)) {
                var configMap = DBConfigMap.builder()
                        .namespace(namespace)
                        .name(name)
                        .data(data)
                        .build();
                configMap.setCreatedAt(now);
                configMap.setUpdatedAt(now);
                log.info("creating config map: {}", configMap);
                list.add(configMap);
                continue;
            }

            if (List.of(env.getActiveProfiles()).contains("dev")) {
                var configMap = get(namespace, name);
                configMap.setData(data);
                configMap.setUpdatedAt(now);
                log.info("updating config map: {}", configMap);
                list.add(configMap);
            }
        }
        if (!list.isEmpty()) {
            repository.saveAll(list);
        }
    }
}
