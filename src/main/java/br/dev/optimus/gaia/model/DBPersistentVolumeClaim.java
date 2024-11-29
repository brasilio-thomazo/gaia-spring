package br.dev.optimus.gaia.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import br.dev.optimus.gaia.AccessMode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "persistent_volume_claims", schema = "gaia", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "namespace", "name" }) })
public class DBPersistentVolumeClaim {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String namespace;
    private String name;
    private String capacity;
    @Column(name = "access_modes")
    @JsonProperty("access_mode")
    private AccessMode accessMode;
    @Column(name = "volume_name")
    @JsonProperty("volume_name")
    private String volumeName;
    @Column(name = "created_at")
    @JsonProperty("created_at")
    private long createdAt;
    @Column(name = "updated_at")
    @JsonProperty("updated_at")
    private long updatedAt;
    @Column(name = "deleted_at")
    @JsonProperty("deleted_at")
    private Long deletedAt;

    public static class Builder {
        private String namespace;
        private String name;
        private String capacity;
        private AccessMode accessMode;
        private String volumeName;

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

        public Builder volumeName(String volumeName) {
            this.volumeName = volumeName.toLowerCase();
            return this;
        }

        public DBPersistentVolumeClaim build() {
            return new DBPersistentVolumeClaim(this);
        }
    }

    public DBPersistentVolumeClaim() {
    }

    public DBPersistentVolumeClaim(Builder builder) {
        this.namespace = builder.namespace;
        this.name = builder.name;
        this.capacity = builder.capacity;
        this.accessMode = builder.accessMode;
        this.volumeName = builder.volumeName;
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

    public String getVolumeName() {
        return volumeName;
    }

    public void setVolumeName(String volumeName) {
        this.volumeName = volumeName.toLowerCase();
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

    public void setDeletedAt(Long deletedAt) {
        this.deletedAt = deletedAt;
    }

}
