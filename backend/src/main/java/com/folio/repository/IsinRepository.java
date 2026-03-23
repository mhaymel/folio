package com.folio.repository;

import com.folio.model.Isin;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface IsinRepository extends JpaRepository<Isin, Integer> {
    Optional<Isin> findByIsin(String isin);
}