package com.folio.repository;

import com.folio.model.IsinQuote;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface IsinQuoteRepository extends JpaRepository<IsinQuote, Integer> {
    Optional<IsinQuote> findByIsinId(Integer isinId);
}