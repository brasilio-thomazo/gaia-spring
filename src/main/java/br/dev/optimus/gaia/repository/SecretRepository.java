package br.dev.optimus.gaia.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.dev.optimus.gaia.model.DBSecret;

public interface SecretRepository extends JpaRepository<DBSecret, Long> {
    boolean existsByNamespaceAndName(String namespace, String name);

    List<DBSecret> findByNamespace(String namespace);

    Optional<DBSecret> findByNamespaceAndName(String namespace, String name);

    List<DBSecret> findByDeletedAtIsNull();
}
