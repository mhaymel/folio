package com.folio.model;

public final class DepotBuilder {
    private Integer id;
    private String name;
    public DepotBuilder id(Integer id) { this.id = id; return this; }
    public DepotBuilder name(String name) { this.name = name; return this; }
    public DepotEntity build() { return new DepotEntity(id, name); }
}