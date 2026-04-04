package com.folio.model;

public final class SettingBuilder {
    private Integer id;
    private String key;
    private String value;
    public SettingBuilder id(Integer id) { this.id = id; return this; }
    public SettingBuilder key(String key) { this.key = key; return this; }
    public SettingBuilder value(String value) { this.value = value; return this; }
    public SettingEntity build() { return new SettingEntity(id, key, value); }
}