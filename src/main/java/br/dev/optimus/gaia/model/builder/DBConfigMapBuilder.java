package br.dev.optimus.gaia.model.builder;

import java.util.Map;

import br.dev.optimus.gaia.model.DBConfigMap;

public final class DBConfigMapBuilder {
    private String namespace;
    private String name;
    private Map<String, String> data;

    public DBConfigMapBuilder namespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    public DBConfigMapBuilder name(String name) {
        this.name = name;
        return this;
    }

    public DBConfigMapBuilder data(Map<String, String> data) {
        this.data = data;
        return this;
    }

    public DBConfigMap build() {
        return new DBConfigMap(namespace, name, data);
    }
}
