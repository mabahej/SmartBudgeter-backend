package com.smartbudgeter.demo.repository;

import com.smartbudgeter.demo.model.AlertSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AlertSettingsRepository extends JpaRepository<AlertSettings, Long> {
    Optional<AlertSettings> findByUserId(Long userId);
}