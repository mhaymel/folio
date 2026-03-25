package com.folio.repository;

import com.folio.model.TickerSymbol;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TickerSymbolRepository extends JpaRepository<TickerSymbol, Integer> {
    Optional<TickerSymbol> findBySymbol(String symbol);
}

