package com.folio.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import static java.util.Objects.requireNonNull;

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
        this.isin = requireNonNull(isin);
        this.name = requireNonNull(name);
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public IsinEntity getIsin() { return isin; }
    public void setIsin(IsinEntity isin) { this.isin = isin; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

}