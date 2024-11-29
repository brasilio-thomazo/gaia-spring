package br.dev.optimus.gaia.model.builder;

import java.util.List;
import java.util.Map;

import br.dev.optimus.gaia.model.DBSecret;

public final class DBSecretBuilder {
    private String namespace;
    private String name;
    private List<String> keys;

    public DBSecretBuilder namespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    public DBSecretBuilder name(String name) {
        this.name = name;
        return this;
    }

    public DBSecretBuilder keys(List<String> keys) {
        this.keys = keys;
        return this;
    }

    public DBSecretBuilder keys(Map<String, String> data) {
        this.keys = data.keySet().stream().toList();
        return this;
    }

    public DBSecret build() {
        return new DBSecret(namespace, name, keys);
    }
}
