package com.folio.model;

import jakarta.persistence.*;

@Entity
@Table(name = "isin_name", uniqueConstraints = @UniqueConstraint(columnNames = {"isin_id", "name"}))
public final class IsinNameEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "isin_id", nullable = false)
    private IsinEntity isin;

    @Column(nullable = false, length = 255)
    private String name;

    public IsinNameEntity() {}

    public IsinNameEntity(Integer id, IsinEntity isin, String name) {
        this.id = id;
        this.isin = isin;
        this.name = name;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public IsinEntity getIsin() { return isin; }
    public void setIsin(IsinEntity isin) { this.isin = isin; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public static IsinNameBuilder builder() { return new IsinNameBuilder(); }
}