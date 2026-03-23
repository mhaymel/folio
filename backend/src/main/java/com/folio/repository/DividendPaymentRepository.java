package com.folio.repository;

import com.folio.model.DividendPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DividendPaymentRepository extends JpaRepository<DividendPayment, Integer> {
    @Modifying
    @Query("DELETE FROM DividendPayment dp WHERE dp.depot.id = :depotId")
    void deleteByDepotId(@Param("depotId") Integer depotId);
}