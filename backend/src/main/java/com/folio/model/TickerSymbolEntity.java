package com.folio.model;

import jakarta.persistence.*;

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
        this.symbol = symbol;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public static TickerSymbolBuilder builder() { return new TickerSymbolBuilder(); }
}
