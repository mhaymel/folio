package com.folio.repository;

import com.folio.model.Dividend;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DividendRepository extends JpaRepository<Dividend, Integer> {
    Optional<Dividend> findByIsinId(Integer isinId);
}