package com.folio.repository;

import com.folio.model.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Collection;
import java.util.Optional;
import java.util.List;

public interface BranchRepository extends JpaRepository<Branch, Integer> {
    Optional<Branch> findByName(String name);
    List<Branch> findByNameIn(Collection<String> names);
    List<Branch> findAllByOrderByNameAsc();
}