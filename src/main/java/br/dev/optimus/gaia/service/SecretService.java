package br.dev.optimus.gaia.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import br.dev.optimus.gaia.exception.BadRequestException;
import br.dev.optimus.gaia.exception.NotFoundException;
import br.dev.optimus.gaia.model.DBSecret;
import br.dev.optimus.gaia.repository.SecretRepository;
import br.dev.optimus.gaia.request.SecretRequest;
import io.fabric8.kubernetes.client.KubernetesClient;

@Service
public class SecretService {
    private final KubernetesClient client;
    private final SecretRepository repository;
    private final Environment env;
    private final Logger log = LoggerFactory.getLogger(ConfigMapService.class);
    private final String namespace;

    public SecretService(KubernetesClient client, SecretRepository repository, Environment env) {
        this.client = client;
        this.repository = repository;
        this.env = env;
        this.namespace = env.getProperty("kubernetes.namespace", "default");
    }

    private void validate(DBSecret data) {
        if (data.getName() == null || data.getName().isEmpty()) {
            throw new BadRequestException("secret name is required", "name");
        }

        if (data.getKeys() == null || data.getKeys().isEmpty()) {
            throw new BadRequestException("secret data is required", "data");
        }

        if (data.getNamespace() == null || data.getNamespace().isEmpty()) {
            data.setNamespace(namespace);
        }
    }

    public DBSecret get(Long id) {
        log.info("get secret: {}", id);
        return repository.findById(id).orElseThrow(() -> new NotFoundException("secret not found"));
    }

    public DBSecret get(String namespace, String name) {
        return repository.findByNamespaceAndName(namespace, name)
                .orElseThrow(() -> new NotFoundException("secret not found"));
    }

    public List<DBSecret> list() {
        return repository.findByDeletedAtIsNull();
    }

    public DBSecret create(DBSecret data, Map<String, String> secrets) {
        var now = Instant.now().getEpochSecond();
        validate(data);
        data.setCreatedAt(now);
        data.setUpdatedAt(now);
        client.secrets().resource(data.secret(secrets)).create();
        log.info("creating secret: {}", data);
        return repository.save(data);
    }

    public DBSecret create(SecretRequest.Create request) {
        var data = DBSecret.builder()
                .name(request.name())
                .namespace(request.namespace())
                .keys(request.data())
                .build();
        return create(data, request.data());
    }

    public DBSecret update(DBSecret data, Map<String, String> secrets) {
        var now = Instant.now().getEpochSecond();
        validate(data);
        data.setUpdatedAt(now);
        client.secrets().resource(data.secret(secrets)).update();
        log.info("updating secret: {}", data);
        return repository.save(data);
    }

    public DBSecret update(Long id, SecretRequest.Update request) {
        var data = get(id);
        data.setKeys(request.data());
        return update(data, request.data());
    }

    public void delete(Long id) {
        log.info("deleting secret: {}", id);
        var data = get(id);
        client.secrets().resource(data.secret()).delete();
        data.setDeletedAt(Instant.now().getEpochSecond());
        repository.save(data);
    }

    public void sync() {
        log.info("syncing secrets");
        var list = new ArrayList<DBSecret>();
        for (var s : client.secrets().inNamespace(namespace).list().getItems()) {
            var name = s.getMetadata().getName();
            var data = s.getData();
            var now = Instant.now().getEpochSecond();
            if (!repository.existsByNamespaceAndName(namespace, name)) {
                var secret = DBSecret.builder()
                        .name(name)
                        .namespace(namespace)
                        .keys(data)
                        .build();
                secret.setCreatedAt(now);
                secret.setUpdatedAt(now);
                log.info("creating secret: {}", secret);
                list.add(secret);
                continue;
            }
            if (List.of(env.getActiveProfiles()).contains("dev")) {
                var secret = get(namespace, name);
                secret.setUpdatedAt(now);
                secret.setKeys(data);
                log.info("updating secret: {}", secret);
                list.add(secret);
            }
        }
        if (!list.isEmpty()) {
            repository.saveAll(list);
        }
    }
}
