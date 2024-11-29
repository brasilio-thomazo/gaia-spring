package br.dev.optimus.gaia.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import br.dev.optimus.gaia.service.ConfigMapService;
import br.dev.optimus.gaia.service.PersistentVolumeClaimService;
import br.dev.optimus.gaia.service.PersistentVolumeService;
import br.dev.optimus.gaia.service.SecretService;

@Configuration
public class InitConfig {
    private final PersistentVolumeService pvService;
    private final PersistentVolumeClaimService pvcService;
    private final ConfigMapService configMapService;
    private final SecretService secretService;

    public InitConfig(PersistentVolumeService pvService, PersistentVolumeClaimService pvcService,
            ConfigMapService configMapService, SecretService secretService) {
        this.pvService = pvService;
        this.pvcService = pvcService;
        this.configMapService = configMapService;
        this.secretService = secretService;
    }

    @Bean
    CommandLineRunner init() {
        return args -> {
            pvService.sync();
            pvcService.sync();
            configMapService.sync();
            secretService.sync();
        };
    }
}
