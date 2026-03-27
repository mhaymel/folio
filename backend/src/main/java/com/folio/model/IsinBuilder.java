package com.folio.model;

public final class IsinBuilder {
    private Integer id;
    private String isin;
    public IsinBuilder id(Integer id) { this.id = id; return this; }
    public IsinBuilder isin(String isin) { this.isin = isin; return this; }
    public Isin build() { return new Isin(id, isin); }
}