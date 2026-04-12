package com.folio.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.folio.domain.Isin;

import java.time.LocalDateTime;

public final class DividendPaymentDto {
    private DividendPaymentIdentity identity;
    private DividendPaymentSource source;
    private Double value;

    public DividendPaymentDto() {
        this.identity = new DividendPaymentIdentity(null, null, null);
        this.source = new DividendPaymentSource(null, null, null);
    }

    public DividendPaymentDto(DividendPaymentIdentity identity, DividendPaymentSource source, Double value) {
        this.identity = identity;
        this.source = source;
        this.value = value;
    }

    @JsonUnwrapped
    public DividendPaymentIdentity getIdentity() { return identity; }
    @JsonUnwrapped
    public DividendPaymentSource getSource() { return source; }

    public Integer getId() { return identity.id(); }
    public void setId(Integer id) { this.identity = new DividendPaymentIdentity(id, identity.timestamp(), identity.rawTimestamp()); }
    public String getTimestamp() { return identity.timestamp(); }
    public void setTimestamp(String timestamp) { this.identity = new DividendPaymentIdentity(identity.id(), timestamp, identity.rawTimestamp()); }
    @JsonIgnore
    public LocalDateTime getRawTimestamp() { return identity.rawTimestamp(); }
    public void setRawTimestamp(LocalDateTime rawTimestamp) { this.identity = new DividendPaymentIdentity(identity.id(), identity.timestamp(), rawTimestamp); }
    public Isin getIsin() { return source.isin(); }
    public void setIsin(Isin isin) { this.source = new DividendPaymentSource(isin, source.name(), source.depot()); }
    public String getName() { return source.name(); }
    public void setName(String name) { this.source = new DividendPaymentSource(source.isin(), name, source.depot()); }
    public String getDepot() { return source.depot(); }
    public void setDepot(String depot) { this.source = new DividendPaymentSource(source.isin(), source.name(), depot); }
    public Double getValue() { return value; }
    public void setValue(Double value) { this.value = value; }

}
