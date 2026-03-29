package com.folio.repository;

import com.folio.model.IsinName;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Collection;
import java.util.List;

public interface IsinNameRepository extends JpaRepository<IsinName, Integer> {
    boolean existsByIsinIdAndName(Integer isinId, String name);
    List<IsinName> findByIsinIdIn(Collection<Integer> isinIds);
}