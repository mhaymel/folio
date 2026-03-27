package com.folio.model;

public final class QuoteProviderBuilder {
    private Integer id;
    private String name;
    public QuoteProviderBuilder id(Integer id) { this.id = id; return this; }
    public QuoteProviderBuilder name(String name) { this.name = name; return this; }
    public QuoteProvider build() { return new QuoteProvider(id, name); }
}