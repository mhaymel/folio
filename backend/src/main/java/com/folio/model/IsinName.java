package com.folio.model;

import jakarta.persistence.*;

@Entity
@Table(name = "isin_name", uniqueConstraints = @UniqueConstraint(columnNames = {"isin_id", "name"}))
public final class IsinName {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
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

    public static IsinNameBuilder builder() { return new IsinNameBuilder(); }
}