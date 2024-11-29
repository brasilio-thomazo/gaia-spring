package br.dev.optimus.gaia.model;

import java.util.Base64;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import br.dev.optimus.gaia.model.builder.DBSecretBuilder;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "secrets", schema = "gaia", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "namespace", "name" }) })
public class DBSecret {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String namespace;
    private String name;
    private List<String> keys;
    @JsonProperty("created_at")
    private Long createdAt;
    @JsonProperty("updated_at")
    private Long updatedAt;
    @JsonProperty("deleted_at")
    private Long deletedAt;

    public DBSecret() {
    }

    public DBSecret(String namespace, String name, List<String> keys) {
        this.namespace = namespace;
        this.name = name;
        this.keys = keys;
    }

    public static DBSecretBuilder builder() {
        return new DBSecretBuilder();
    }

    public Secret secret(Map<String, String> data) {
        data.forEach((k, v) -> data.put(k, Base64.getEncoder().encodeToString(v.getBytes())));
        var secret = new SecretBuilder()
                .withType("Opaque")
                .withNewMetadata()
                .withNamespace(namespace)
                .withName(name)
                .endMetadata()
                .withData(data)
                .build();
        return secret;
    }

    public Secret secret() {
        var secret = new SecretBuilder()
                .withNewMetadata()
                .withNamespace(namespace)
                .withName(name)
                .endMetadata()
                .build();
        return secret;
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

    public List<String> getKeys() {
        return keys;
    }

    public void setKeys(List<String> keys) {
        this.keys = keys;
    }

    public void setKeys(Map<String, String> data) {
        this.keys = data.keySet().stream().toList();
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
