package com.folio.repository;

import com.folio.model.SettingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SettingRepository extends JpaRepository<SettingEntity, Integer> {
    Optional<SettingEntity> findByKey(String key);
}