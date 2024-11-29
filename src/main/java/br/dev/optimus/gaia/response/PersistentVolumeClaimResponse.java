package br.dev.optimus.gaia.response;

import br.dev.optimus.gaia.model.DBPersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;

public record PersistentVolumeClaimResponse(PersistentVolumeClaim pvc, DBPersistentVolumeClaim data) {

}
