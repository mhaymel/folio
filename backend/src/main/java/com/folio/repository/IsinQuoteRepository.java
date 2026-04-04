package com.folio.repository;

import com.folio.model.IsinQuoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface IsinQuoteRepository extends JpaRepository<IsinQuoteEntity, Integer> {
    Optional<IsinQuoteEntity> findByIsinId(Integer isinId);
}