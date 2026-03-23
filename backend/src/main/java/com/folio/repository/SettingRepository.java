package com.folio.repository;

import com.folio.model.Setting;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SettingRepository extends JpaRepository<Setting, Integer> {
    Optional<Setting> findByKey(String key);
}