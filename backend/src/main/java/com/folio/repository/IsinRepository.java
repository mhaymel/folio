package com.folio.repository;

import com.folio.model.IsinEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface IsinRepository extends JpaRepository<IsinEntity, Integer> {
    Optional<IsinEntity> findByIsin(String isin);
    List<IsinEntity> findByIsinIn(Collection<String> isins);
}