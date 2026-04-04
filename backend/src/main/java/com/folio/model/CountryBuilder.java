package com.folio.model;

public final class CountryBuilder {
    private Integer id;
    private String name;
    public CountryBuilder id(Integer id) { this.id = id; return this; }
    public CountryBuilder name(String name) { this.name = name; return this; }
    public CountryEntity build() { return new CountryEntity(id, name); }
}