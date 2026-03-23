package com.folio.model;

import jakarta.persistence.*;

@Entity
@Table(name = "branch")
public class Branch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    public Branch() {}

    public Branch(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Integer id;
        private String name;
        public Builder id(Integer id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Branch build() { return new Branch(id, name); }
    }
}