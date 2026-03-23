package com.folio.model;

import jakarta.persistence.*;

@Entity
@Table(name = "isin_name", uniqueConstraints = @UniqueConstraint(columnNames = {"isin_id", "name"}))
public class IsinName {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "isin_id", nullable = false)
    private Isin isin;

    @Column(nullable = false, length = 255)
    private String name;

    public IsinName() {}

    public IsinName(Integer id, Isin isin, String name) {
        this.id = id;
        this.isin = isin;
        this.name = name;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Isin getIsin() { return isin; }
    public void setIsin(Isin isin) { this.isin = isin; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Integer id;
        private Isin isin;
        private String name;
        public Builder id(Integer id) { this.id = id; return this; }
        public Builder isin(Isin isin) { this.isin = isin; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public IsinName build() { return new IsinName(id, isin, name); }
    }
}