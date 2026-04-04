package com.folio.repository;

import com.folio.model.CurrencyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CurrencyRepository extends JpaRepository<CurrencyEntity, Integer> {
    Optional<CurrencyEntity> findByName(String name);
    List<CurrencyEntity> findByNameIn(Collection<String> names);
    List<CurrencyEntity> findAllByOrderByNameAsc();
}