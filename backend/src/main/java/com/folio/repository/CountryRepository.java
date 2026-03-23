package com.folio.repository;

import com.folio.model.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface CountryRepository extends JpaRepository<Country, Integer> {
    Optional<Country> findByName(String name);
    List<Country> findAllByOrderByNameAsc();
}