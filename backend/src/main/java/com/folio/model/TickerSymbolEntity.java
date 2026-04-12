package com.folio.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import static java.util.Objects.requireNonNull;

@Entity
@Table(name = "ticker_symbol")
public final class TickerSymbolEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 20)
    private String symbol;

    public TickerSymbolEntity() {}

    public TickerSymbolEntity(Integer id, String symbol) {
        this.id = id;
        this.symbol = requireNonNull(symbol);
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

}
