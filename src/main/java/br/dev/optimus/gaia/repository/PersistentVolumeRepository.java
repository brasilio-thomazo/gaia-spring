package br.dev.optimus.gaia.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.dev.optimus.gaia.model.DBPersistentVolume;

@Repository
public interface PersistentVolumeRepository extends JpaRepository<DBPersistentVolume, Long> {
    boolean existsByNamespaceAndName(String namespace, String name);

    List<DBPersistentVolume> findByDeletedAtIsNull();

    Optional<DBPersistentVolume> findByNamespaceAndName(String namespace, String name);
}
