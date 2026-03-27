package com.folio.model;

import jakarta.persistence.*;

@Entity
@Table(name = "settings")
public final class Setting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "\"key\"", nullable = false, unique = true, length = 100)
    private String key;

    @Column(name = "\"value\"", nullable = false, length = 500)
    private String value;

    public Setting() {}

    public Setting(Integer id, String key, String value) {
        this.id = id;
        this.key = key;
        this.value = value;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

    public static SettingBuilder builder() { return new SettingBuilder(); }
}