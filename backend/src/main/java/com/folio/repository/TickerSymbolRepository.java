package com.folio.repository;

import com.folio.model.TickerSymbolEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TickerSymbolRepository extends JpaRepository<TickerSymbolEntity, Integer> {
    Optional<TickerSymbolEntity> findBySymbol(String symbol);
}
