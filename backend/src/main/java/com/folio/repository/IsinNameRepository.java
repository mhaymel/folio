package com.folio.repository;

import com.folio.model.IsinName;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IsinNameRepository extends JpaRepository<IsinName, Integer> {
    boolean existsByIsinIdAndName(Integer isinId, String name);
}