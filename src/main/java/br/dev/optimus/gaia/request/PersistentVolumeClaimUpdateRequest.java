package br.dev.optimus.gaia.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import br.dev.optimus.gaia.AccessMode;

public record PersistentVolumeClaimUpdateRequest(
        String capacity,
        @JsonProperty("access_mode") AccessMode accessMode,
        @JsonProperty("volume_name") String volumeName) {

}
