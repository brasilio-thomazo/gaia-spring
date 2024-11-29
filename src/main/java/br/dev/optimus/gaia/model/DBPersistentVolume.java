package br.dev.optimus.gaia.model;

import java.util.HashMap;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.fasterxml.jackson.annotation.JsonProperty;

import br.dev.optimus.gaia.AccessMode;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Table(name = "persistent_volumes", schema = "gaia", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "namespace", "name" }) })
@Entity
public class DBPersistentVolume {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String namespace;
    private String name;
    private String capacity;
    @JsonProperty("access_mode")
    private AccessMode accessMode;
    private String type;
    @JdbcTypeCode(SqlTypes.JSON)
    private HashMap<String, String> configs;
    @JsonProperty("created_at")
    private long createdAt;
    @JsonProperty("updated_at")
    private long updatedAt;
    @JsonProperty("deleted_at")
    private Long deletedAt;

    public static class Builder {
        private String namespace;
        private String name;
        private String capacity;
        private AccessMode accessMode;
        private String type;
        private HashMap<String, String> configs;

        public Builder namespace(String namespace) {
            this.namespace = namespace == null || namespace.isEmpty() ? "default" : namespace.toLowerCase();
            return this;
        }

        public Builder name(String name) {
            this.name = name.toLowerCase();
            return this;
        }

        public Builder capacity(String capacity) {
            this.capacity = capacity;
            return this;
        }

        public Builder accessMode(AccessMode accessMode) {
            this.accessMode = accessMode;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder configs(HashMap<String, String> configs) {
            this.configs = configs;
            return this;
        }

        public DBPersistentVolume build() {
            return new DBPersistentVolume(this);
        }
    }

    public DBPersistentVolume() {
    }

    public DBPersistentVolume(Builder builder) {
        this.namespace = builder.namespace;
        this.name = builder.name;
        this.capacity = builder.capacity;
        this.accessMode = builder.accessMode;
        this.type = builder.type;
        this.configs = builder.configs;
    }

    public static Builder builder() {
        return new Builder();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace == null || namespace.isEmpty() ? "default" : namespace.toLowerCase();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name.toLowerCase();
    }

    public String getCapacity() {
        return capacity;
    }

    public void setCapacity(String capacity) {
        this.capacity = capacity;
    }

    public AccessMode getAccessMode() {
        return accessMode;
    }

    public void setAccessMode(AccessMode accessMode) {
        this.accessMode = accessMode;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public HashMap<String, String> getConfigs() {
        return configs;
    }

    public void setConfigs(HashMap<String, String> configs) {
        this.configs = configs;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(long deletedAt) {
        this.deletedAt = deletedAt;
    }
}
