package com.folio.repository;

import com.folio.model.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CurrencyRepository extends JpaRepository<Currency, Integer> {
    Optional<Currency> findByName(String name);
    List<Currency> findAllByOrderByNameAsc();
}