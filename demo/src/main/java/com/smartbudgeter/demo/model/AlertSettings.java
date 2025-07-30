package com.smartbudgeter.demo.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "alert_settings")
public class AlertSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "warning_threshold", nullable = false)
    private BigDecimal warningThreshold;

    // Constructors
    public AlertSettings() {}

    public AlertSettings(Long userId, BigDecimal warningThreshold) {
        this.userId = userId;
        this.warningThreshold = warningThreshold;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public BigDecimal getWarningThreshold() { return warningThreshold; }
    public void setWarningThreshold(BigDecimal warningThreshold) { this.warningThreshold = warningThreshold; }
}