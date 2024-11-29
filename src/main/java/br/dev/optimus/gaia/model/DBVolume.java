package br.dev.optimus.gaia.model;

import java.util.Map;

import io.fabric8.kubernetes.api.model.HostPathVolumeSource;
import io.fabric8.kubernetes.api.model.HostPathVolumeSourceBuilder;
import io.fabric8.kubernetes.api.model.NFSVolumeSource;
import io.fabric8.kubernetes.api.model.NFSVolumeSourceBuilder;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaimVolumeSource;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaimVolumeSourceBuilder;
import io.fabric8.kubernetes.api.model.Volume;
import io.fabric8.kubernetes.api.model.VolumeBuilder;

public class DBVolume {
    private String name;
    private boolean readOnly;
    private Type type;
    private Map<String, String> configs;

    public enum Type {
        PersistentVolumeClaim, NFS, HostPath
    }

    private PersistentVolumeClaimVolumeSource pvcSource() {
        if (configs == null) {
            return null;
        }
        if (configs.get("claim_name") == null) {
            return null;
        }
        return new PersistentVolumeClaimVolumeSourceBuilder()
                .withClaimName(configs.get("claim_name"))
                .withReadOnly(readOnly)
                .build();
    }

    private NFSVolumeSource nfsSource() {
        if (configs == null) {
            return null;
        }
        if (configs.get("server") == null || configs.get("path") == null) {
            return null;
        }
        return new NFSVolumeSourceBuilder()
                .withServer(configs.get("server"))
                .withPath(configs.get("path"))
                .withReadOnly(readOnly)
                .build();
    }

    private HostPathVolumeSource hostPathSource() {
        if (configs == null) {
            return null;
        }
        if (configs.get("path") == null) {
            return null;
        }
        return new HostPathVolumeSourceBuilder()
                .withPath(configs.get("path"))
                .build();
    }

    public Volume volume() {
        var builder = new VolumeBuilder();
        builder.withName(name);
        return switch (type) {
            case PersistentVolumeClaim -> builder.withPersistentVolumeClaim(pvcSource()).build();
            case NFS -> builder.withNfs(nfsSource()).build();
            case HostPath -> builder.withHostPath(hostPathSource()).build();
            default -> throw new UnsupportedOperationException();
        };
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Map<String, String> getConfigs() {
        return configs;
    }

    public void setConfigs(Map<String, String> configs) {
        this.configs = configs;
    }

    @Override
    public String toString() {
        return String.format("Volume [name=%s, readOnly=%s, type=%s, configs=%s]", name, readOnly, type, configs);
    }
}
