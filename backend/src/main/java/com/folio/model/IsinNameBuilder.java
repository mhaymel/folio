package com.folio.model;

public final class IsinNameBuilder {
    private Integer id;
    private IsinEntity isin;
    private String name;

    public IsinNameBuilder id(Integer id) { this.id = id; return this; }
    public IsinNameBuilder isin(IsinEntity isin) { this.isin = isin; return this; }
    public IsinNameBuilder name(String name) { this.name = name; return this; }
    public IsinNameEntity build() { return new IsinNameEntity(id, isin, name); }
}

