package com.folio.repository;

import com.folio.model.IsinNameEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Collection;
import java.util.List;

public interface IsinNameRepository extends JpaRepository<IsinNameEntity, Integer> {
    boolean existsByIsinIdAndName(Integer isinId, String name);
    List<IsinNameEntity> findByIsinIdIn(Collection<Integer> isinIds);
}