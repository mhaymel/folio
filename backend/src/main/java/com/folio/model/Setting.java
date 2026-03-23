package com.folio.model;

import jakarta.persistence.*;

@Entity
@Table(name = "settings")
public class Setting {
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

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Integer id;
        private String key;
        private String value;
        public Builder id(Integer id) { this.id = id; return this; }
        public Builder key(String key) { this.key = key; return this; }
        public Builder value(String value) { this.value = value; return this; }
        public Setting build() { return new Setting(id, key, value); }
    }
}