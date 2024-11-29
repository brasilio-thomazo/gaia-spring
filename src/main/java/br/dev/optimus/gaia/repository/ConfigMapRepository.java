package br.dev.optimus.gaia.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.dev.optimus.gaia.model.DBConfigMap;

@Repository
public interface ConfigMapRepository extends JpaRepository<DBConfigMap, Long> {
    boolean existsByNamespaceAndName(String namespace, String name);

    List<DBConfigMap> findByNamespace(String namespace);

    Optional<DBConfigMap> findByNamespaceAndName(String namespace, String name);

    List<DBConfigMap> findByDeletedAtIsNull();
}
