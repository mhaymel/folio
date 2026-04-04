package com.folio.repository;

import com.folio.model.DividendPaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DividendPaymentRepository extends JpaRepository<DividendPaymentEntity, Integer> {
    @Modifying
    @Query("DELETE FROM DividendPaymentEntity dp WHERE dp.depot.id = :depotId")
    void deleteByDepotId(@Param("depotId") Integer depotId);
}