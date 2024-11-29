package br.dev.optimus.gaia.service;

import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import br.dev.optimus.gaia.exception.BadRequestException;
import br.dev.optimus.gaia.exception.NotFoundException;
import br.dev.optimus.gaia.model.DBStatefulSet;
import br.dev.optimus.gaia.repository.StatefulSetRepository;
import br.dev.optimus.gaia.request.StatefulSetRequest;
import io.fabric8.kubernetes.client.KubernetesClient;

@Service
public class StatefulSetService {
    private final KubernetesClient client;
    private final StatefulSetRepository repository;
    private final Environment env;
    private final Logger log = LoggerFactory.getLogger(StatefulSetService.class);
    @Value("${kubernetes.namespace}")
    private String namespace;

    public StatefulSetService(KubernetesClient client, StatefulSetRepository repository, Environment env) {
        this.client = client;
        this.repository = repository;
        this.env = env;
    }

    private void validate(DBStatefulSet data) {
        log.info("validating statefulset: {}", data);
        if (data.getNamespace() == null || data.getNamespace().isEmpty()) {
            data.setNamespace(namespace);
        }

        if (data.getReplicas() == null || data.getReplicas() <= 0) {
            data.setReplicas(1);
        }

        if (data.getName() == null || data.getName().isEmpty()) {
            throw new BadRequestException("statefulset name is required", "name");
        }

        if (data.getContainers() == null || data.getContainers().isEmpty()) {
            throw new BadRequestException("statefulset containers is required", "containers");
        }
    }

    public List<DBStatefulSet> list() {
        log.info("listing statefulsets");
        return repository.findByDeletedAtIsNull();
    }

    public DBStatefulSet get(Long id) {
        log.info("get statefulset: {}", id);
        return repository.findById(id).orElseThrow(() -> new NotFoundException("statefulset not found"));
    }

    public DBStatefulSet create(DBStatefulSet data) {
        var now = Instant.now().getEpochSecond();
        validate(data);
        if (repository.existsByNamespaceAndName(data.getNamespace(), data.getName())) {
            throw new BadRequestException("statefulset name already exists", "name");
        }
        client.apps().statefulSets().resource(data.statefulSet()).create();
        data.setCreatedAt(now);
        data.setUpdatedAt(now);
        log.info("creating statefulset: {}", data);
        return repository.save(data);
    }

    public DBStatefulSet create(StatefulSetRequest.Create request) {
        var data = DBStatefulSet.builder()
                .namespace(request.namespace())
                .name(request.name())
                .replicas(request.replicas())
                .initContainers(request.initContainers())
                .containers(request.containers())
                .volumes(request.volumes())
                .build();
        return create(data);
    }

    public DBStatefulSet update(DBStatefulSet data) {
        validate(data);
        client.apps().statefulSets().resource(data.statefulSet()).update();
        data.setUpdatedAt(Instant.now().getEpochSecond());
        log.info("updating statefulset: {}", data);
        return repository.save(data);
    }

    public DBStatefulSet update(Long id, StatefulSetRequest.Update request) {
        var data = get(id);
        data.setReplicas(request.replicas());
        data.setInitContainers(request.initContainers());
        data.setContainers(request.containers());
        data.setVolumes(request.volumes());
        return update(data);
    }

    public void delete(Long id) {
        log.info("deleting statefulset: {}", id);
        var data = get(id);
        client.apps().statefulSets().resource(data.statefulSet()).delete();
        data.setDeletedAt(Instant.now().getEpochSecond());
        repository.save(data);
    }
}
