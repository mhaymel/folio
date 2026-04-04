package com.folio.repository;

import com.folio.model.BranchEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Collection;
import java.util.Optional;
import java.util.List;

public interface BranchRepository extends JpaRepository<BranchEntity, Integer> {
    Optional<BranchEntity> findByName(String name);
    List<BranchEntity> findByNameIn(Collection<String> names);
    List<BranchEntity> findAllByOrderByNameAsc();
}