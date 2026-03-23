package com.folio.repository;

import com.folio.model.QuoteProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface QuoteProviderRepository extends JpaRepository<QuoteProvider, Integer> {
    Optional<QuoteProvider> findByName(String name);
}