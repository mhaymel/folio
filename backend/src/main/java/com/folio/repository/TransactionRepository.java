package com.folio.repository;

import com.folio.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransactionRepository extends JpaRepository<Transaction, Integer>, JpaSpecificationExecutor<Transaction> {
    @Modifying
    @Query("DELETE FROM Transaction t WHERE t.depot.id = :depotId")
    void deleteByDepotId(@Param("depotId") Integer depotId);
}