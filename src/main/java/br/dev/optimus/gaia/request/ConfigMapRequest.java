package br.dev.optimus.gaia.request;

import java.util.Map;

public final class ConfigMapRequest {
    public record Create(String namespace, String name, Map<String, String> data) {
    }

    public record Update(Map<String, String> data) {
    }
}
