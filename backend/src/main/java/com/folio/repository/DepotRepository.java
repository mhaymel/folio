package com.folio.repository;

import com.folio.model.DepotEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DepotRepository extends JpaRepository<DepotEntity, Integer> {
    Optional<DepotEntity> findByName(String name);
    List<DepotEntity> findAllByOrderByNameAsc();
}