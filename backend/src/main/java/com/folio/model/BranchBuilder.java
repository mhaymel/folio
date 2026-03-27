package com.folio.model;

public final class BranchBuilder {
    private Integer id;
    private String name;
    public BranchBuilder id(Integer id) { this.id = id; return this; }
    public BranchBuilder name(String name) { this.name = name; return this; }
    public Branch build() { return new Branch(id, name); }
}