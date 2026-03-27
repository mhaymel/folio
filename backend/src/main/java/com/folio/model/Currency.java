package com.folio.model;

import jakarta.persistence.*;

@Entity
@Table(name = "currency")
public final class Currency {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 3)
    private String name;

    public Currency() {}

    public Currency(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public static CurrencyBuilder builder() { return new CurrencyBuilder(); }
}