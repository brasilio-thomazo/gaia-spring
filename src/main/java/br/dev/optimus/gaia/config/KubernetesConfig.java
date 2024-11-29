package br.dev.optimus.gaia.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;

@Configuration
public class KubernetesConfig {
    @Bean
    KubernetesClient kubernetesClient() {
        return new KubernetesClientBuilder().build();
    }
}
