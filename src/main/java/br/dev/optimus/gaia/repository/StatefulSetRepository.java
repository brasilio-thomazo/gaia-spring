package br.dev.optimus.gaia.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.dev.optimus.gaia.model.DBStatefulSet;

public interface StatefulSetRepository extends JpaRepository<DBStatefulSet, Long> {
    boolean existsByNamespaceAndName(String namespace, String name);

    List<DBStatefulSet> findByNamespace(String namespace);

    List<DBStatefulSet> findByDeletedAtIsNull();

    Optional<DBStatefulSet> findByNamespaceAndName(String namespace, String name);
}
