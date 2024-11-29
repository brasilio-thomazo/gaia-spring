package br.dev.optimus.gaia.model.builder;

import java.util.List;

import br.dev.optimus.gaia.model.DBContainer;
import br.dev.optimus.gaia.model.DBStatefulSet;
import br.dev.optimus.gaia.model.DBVolume;

public final class DBStatefulSetBuilder {
    private String namespace;
    private String name;
    private int replicas;
    private List<DBContainer> initContainers;
    private List<DBContainer> containers;
    private List<DBVolume> volumes;

    public DBStatefulSetBuilder namespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    public DBStatefulSetBuilder name(String name) {
        this.name = name;
        return this;
    }

    public DBStatefulSetBuilder replicas(int replicas) {
        this.replicas = replicas > 0 ? replicas : 1;
        return this;
    }

    public DBStatefulSetBuilder initContainers(List<DBContainer> initContainers) {
        this.initContainers = initContainers;
        return this;
    }

    public DBStatefulSetBuilder containers(List<DBContainer> containers) {
        this.containers = containers;
        return this;
    }

    public DBStatefulSetBuilder volumes(List<DBVolume> volumes) {
        this.volumes = volumes;
        return this;
    }

    public DBStatefulSet build() {
        return new DBStatefulSet(namespace, name, replicas, initContainers, containers, volumes);
    }

}
