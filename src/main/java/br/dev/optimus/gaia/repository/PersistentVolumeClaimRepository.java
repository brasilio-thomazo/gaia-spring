package br.dev.optimus.gaia.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.dev.optimus.gaia.model.DBPersistentVolumeClaim;

@Repository
public interface PersistentVolumeClaimRepository extends JpaRepository<DBPersistentVolumeClaim, Long> {
    boolean existsByNamespaceAndName(String namespace, String name);

    List<DBPersistentVolumeClaim> findByDeletedAtIsNull();

    Optional<DBPersistentVolumeClaim> findByNamespaceAndName(String namespace, String name);

}
