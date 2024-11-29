package br.dev.optimus.gaia.request;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import br.dev.optimus.gaia.model.DBContainer;
import br.dev.optimus.gaia.model.DBVolume;

public final class StatefulSetRequest {
        public record Create(
                        String namespace,
                        String name,
                        Integer replicas,
                        @JsonProperty("init_containers") List<DBContainer> initContainers,
                        List<DBContainer> containers,
                        List<DBVolume> volumes) {
        }

        public record Update(
                        Integer replicas,
                        @JsonProperty("init_containers") List<DBContainer> initContainers,
                        List<DBContainer> containers,
                        List<DBVolume> volumes) {
        }
}
