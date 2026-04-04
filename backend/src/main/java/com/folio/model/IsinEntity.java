package com.folio.model;

import jakarta.persistence.*;

@Entity
@Table(name = "isin")
public final class IsinEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 12)
    private String isin;

    public IsinEntity() {}

    public IsinEntity(Integer id, String isin) {
        this.id = id;
        this.isin = isin;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getIsin() { return isin; }
    public void setIsin(String isin) { this.isin = isin; }

    public static IsinBuilder builder() { return new IsinBuilder(); }
}