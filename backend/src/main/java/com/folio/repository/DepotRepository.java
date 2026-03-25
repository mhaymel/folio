package com.folio.repository;

import com.folio.model.Depot;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DepotRepository extends JpaRepository<Depot, Integer> {
    Optional<Depot> findByName(String name);
    List<Depot> findAllByOrderByNameAsc();
}