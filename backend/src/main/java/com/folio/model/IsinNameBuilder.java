package com.folio.model;

public final class IsinNameBuilder {
    private Integer id;
    private Isin isin;
    private String name;

    public IsinNameBuilder id(Integer id) { this.id = id; return this; }
    public IsinNameBuilder isin(Isin isin) { this.isin = isin; return this; }
    public IsinNameBuilder name(String name) { this.name = name; return this; }
    public IsinName build() { return new IsinName(id, isin, name); }
}

