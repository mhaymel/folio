package com.folio.model;

public final class CurrencyBuilder {
    private Integer id;
    private String name;
    public CurrencyBuilder id(Integer id) { this.id = id; return this; }
    public CurrencyBuilder name(String name) { this.name = name; return this; }
    public Currency build() { return new Currency(id, name); }
}