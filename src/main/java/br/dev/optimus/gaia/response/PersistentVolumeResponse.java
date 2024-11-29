package br.dev.optimus.gaia.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import br.dev.optimus.gaia.model.DBPersistentVolume;
import io.fabric8.kubernetes.api.model.PersistentVolume;

public record PersistentVolumeResponse(DBPersistentVolume data,
        @JsonProperty("persistent_volume") PersistentVolume persistentVolume) {

}
