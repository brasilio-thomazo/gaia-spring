package br.dev.optimus.gaia.model;

import java.util.List;
import java.util.Map;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.fasterxml.jackson.annotation.JsonProperty;

import br.dev.optimus.gaia.model.builder.DBStatefulSetBuilder;
import io.fabric8.kubernetes.api.model.PodSpecBuilder;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.api.model.apps.StatefulSetBuilder;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "statefulsets", schema = "gaia", uniqueConstraints = @UniqueConstraint(columnNames = { "namespace",
        "name" }))
public class DBStatefulSet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String namespace;
    private String name;
    private Integer replicas;
    @JdbcTypeCode(SqlTypes.JSON)
    private List<DBContainer> initContainers;
    @JdbcTypeCode(SqlTypes.JSON)
    private List<DBContainer> containers;
    @JdbcTypeCode(SqlTypes.JSON)
    private List<DBVolume> volumes;
    @JsonProperty("created_at")
    private Long createdAt;
    @JsonProperty("updated_at")
    private Long updatedAt;
    @JsonProperty("deleted_at")
    private Long deletedAt;

    public DBStatefulSet() {
    }

    public DBStatefulSet(String namespace, String name, int replicas, List<DBContainer> initContainers,
            List<DBContainer> containers, List<DBVolume> volumes) {
        this.namespace = namespace;
        this.name = name;
        this.replicas = replicas;
        this.initContainers = initContainers;
        this.containers = containers;
        this.volumes = volumes;
    }

    public static DBStatefulSetBuilder builder() {
        return new DBStatefulSetBuilder();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getReplicas() {
        return replicas;
    }

    public void setReplicas(Integer replicas) {
        this.replicas = replicas;
    }

    public List<DBContainer> getInitContainers() {
        return initContainers;
    }

    public void setInitContainers(List<DBContainer> initContainers) {
        this.initContainers = initContainers;
    }

    public List<DBContainer> getContainers() {
        return containers;
    }

    public void setContainers(List<DBContainer> containers) {
        this.containers = containers;
    }

    public List<DBVolume> getVolumes() {
        return volumes;
    }

    public void setVolumes(List<DBVolume> volumes) {
        this.volumes = volumes;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Long deletedAt) {
        this.deletedAt = deletedAt;
    }

    public StatefulSet statefulSet() {
        var specBuilder = new PodSpecBuilder();
        if (initContainers != null) {
            specBuilder.withInitContainers(initContainers.stream().map(DBContainer::container).toList());
        }
        if (containers != null) {
            specBuilder.withContainers(containers.stream().map(DBContainer::container).toList());
        }
        if (volumes != null) {
            specBuilder.withVolumes(volumes.stream().map(DBVolume::volume).toList());
        }

        return new StatefulSetBuilder()
                .withNewMetadata()
                .withName(name)
                .withNamespace(namespace)
                .endMetadata()
                .withNewSpec()
                .withNewSelector()
                .withMatchLabels(Map.of("app", name))
                .endSelector()
                .withNewTemplate()
                .withNewMetadata()
                .withLabels(Map.of("app", name))
                .endMetadata()
                .withSpec(specBuilder.build())
                .endTemplate()
                .endSpec()
                .build();
    }
}
