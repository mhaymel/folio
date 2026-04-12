package com.folio.model;

import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Embeddable;

import java.time.LocalDateTime;

import static java.util.Objects.requireNonNull;

/**
 * Groups context fields (date, isin, depot) for a transaction entity.
 */
@Embeddable
public final class TransactionContext {
    @Column(name = "\"date\"", nullable = false)
    private LocalDateTime date;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "isin_id", nullable = false)
    private IsinEntity isin;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "depot_id", nullable = false)
    private DepotEntity depot;

    public TransactionContext() {}

    public TransactionContext(LocalDateTime date, IsinEntity isin, DepotEntity depot) {
        this.date = requireNonNull(date);
        this.isin = requireNonNull(isin);
        this.depot = requireNonNull(depot);
    }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }
    public IsinEntity getIsin() { return isin; }
    public void setIsin(IsinEntity isin) { this.isin = isin; }
    public DepotEntity getDepot() { return depot; }
    public void setDepot(DepotEntity depot) { this.depot = depot; }
}
