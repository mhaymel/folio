package com.folio.repository;

import com.folio.model.DividendEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DividendRepository extends JpaRepository<DividendEntity, Integer> {
    Optional<DividendEntity> findByIsinId(Integer isinId);
}