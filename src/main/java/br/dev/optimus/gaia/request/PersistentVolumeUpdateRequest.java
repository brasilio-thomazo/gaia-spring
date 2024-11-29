package br.dev.optimus.gaia.request;

import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonProperty;

import br.dev.optimus.gaia.AccessMode;

public record PersistentVolumeUpdateRequest(
        String capacity,
        @JsonProperty("access_mode") AccessMode accessMode,
        String type,
        HashMap<String, String> configs) {

}
