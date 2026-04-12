package com.folio.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import static java.util.Objects.requireNonNull;

@Entity
@Table(name = "settings")
public final class SettingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "\"key\"", nullable = false, unique = true, length = 100)
    private String key;

    @Column(name = "\"value\"", nullable = false, length = 500)
    private String value;

    public SettingEntity() {}

    public SettingEntity(Integer id, String key, String value) {
        this.id = id;
        this.key = requireNonNull(key);
        this.value = requireNonNull(value);
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

}