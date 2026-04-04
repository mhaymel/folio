package com.folio.repository;

import com.folio.model.CountryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Collection;
import java.util.Optional;
import java.util.List;

public interface CountryRepository extends JpaRepository<CountryEntity, Integer> {
    Optional<CountryEntity> findByName(String name);
    List<CountryEntity> findByNameIn(Collection<String> names);
    List<CountryEntity> findAllByOrderByNameAsc();
}