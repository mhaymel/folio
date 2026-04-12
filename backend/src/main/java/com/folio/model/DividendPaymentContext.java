package com.folio.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.time.LocalDateTime;

import static java.util.Objects.requireNonNull;

/**
 * Groups context fields (timestamp, isin, depot) for a dividend payment entity.
 */
@Embeddable
public final class DividendPaymentContext {
    @Column(name = "\"timestamp\"", nullable = false)
    private LocalDateTime timestamp;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "isin_id", nullable = false)
    private IsinEntity isin;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "depot_id", nullable = false)
    private DepotEntity depot;

    public DividendPaymentContext() {}

    public DividendPaymentContext(LocalDateTime timestamp, IsinEntity isin, DepotEntity depot) {
        this.timestamp = requireNonNull(timestamp);
        this.isin = requireNonNull(isin);
        this.depot = requireNonNull(depot);
    }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public IsinEntity getIsin() { return isin; }
    public void setIsin(IsinEntity isin) { this.isin = isin; }
    public DepotEntity getDepot() { return depot; }
    public void setDepot(DepotEntity depot) { this.depot = depot; }
}
