package com.folio.model;

import jakarta.persistence.*;

@Entity
@Table(name = "ticker_symbol")
public final class TickerSymbol {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "isin_id", unique = true)
    private Isin isin;

    @Column(nullable = false, unique = true, length = 20)
    private String symbol;

    public TickerSymbol() {}

    public TickerSymbol(Integer id, Isin isin, String symbol) {
        this.id = id;
        this.isin = isin;
        this.symbol = symbol;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Isin getIsin() { return isin; }
    public void setIsin(Isin isin) { this.isin = isin; }
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public static TickerSymbolBuilder builder() { return new TickerSymbolBuilder(); }
}

