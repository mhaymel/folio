package com.folio.repository;

import com.folio.model.QuoteProviderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface QuoteProviderRepository extends JpaRepository<QuoteProviderEntity, Integer> {
    Optional<QuoteProviderEntity> findByName(String name);
}