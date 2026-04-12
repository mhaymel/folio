package com.folio.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import static java.util.Objects.requireNonNull;

/**
 * Groups trade value fields (count, sharePrice) for a transaction entity.
 */
@Embeddable
public final class TransactionValues {
    @Column(name = "\"count\"", nullable = false)
    private Double count;

    @Column(name = "share_price", nullable = false)
    private Double sharePrice;

    public TransactionValues() {}

    public TransactionValues(Double count, Double sharePrice) {
        this.count = requireNonNull(count);
        this.sharePrice = requireNonNull(sharePrice);
    }

    public Double getCount() { return count; }
    public void setCount(Double count) { this.count = count; }
    public Double getSharePrice() { return sharePrice; }
    public void setSharePrice(Double sharePrice) { this.sharePrice = sharePrice; }
}
