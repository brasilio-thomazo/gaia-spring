package br.dev.optimus.gaia.model;

import java.util.Map;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.fasterxml.jackson.annotation.JsonProperty;

import br.dev.optimus.gaia.model.builder.DBConfigMapBuilder;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Table(name = "configmaps", schema = "gaia", uniqueConstraints = @UniqueConstraint(columnNames = { "namespace",
        "name" }))
@Entity
public class DBConfigMap {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String namespace;
    private String name;
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, String> data;
    @JsonProperty("created_at")
    private Long createdAt;
    @JsonProperty("updated_at")
    private Long updatedAt;
    @JsonProperty("deleted_at")
    private Long deletedAt;

    public DBConfigMap() {
    }

    public DBConfigMap(String namespace, String name, Map<String, String> data) {
        this.namespace = namespace;
        this.name = name;
        this.data = data;
    }

    public static DBConfigMapBuilder builder() {
        return new DBConfigMapBuilder();
    }

    public ConfigMap configMap() {
        return new ConfigMapBuilder()
                .withNewMetadata()
                .withNamespace(namespace)
                .withName(name)
                .endMetadata()
                .withData(data)
                .build();
    }

    @Override
    public String toString() {
        return String.format("ConfigMap [id=%s, namespace=%s, name=%s, data=%s]", id, namespace, name, data);
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

    public Map<String, String> getData() {
        return data;
    }

    public void setData(Map<String, String> data) {
        this.data = data;
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
}
