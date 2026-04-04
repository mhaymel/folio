package com.folio.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.folio.domain.Isin;
import java.time.LocalDateTime;

public final class DividendPaymentDto {
    private Integer id;
    private String timestamp;
    @JsonIgnore
    private LocalDateTime rawTimestamp;
    private Isin isin;
    private String name;
    private String depot;
    private Double value;

    public DividendPaymentDto() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public LocalDateTime getRawTimestamp() { return rawTimestamp; }
    public void setRawTimestamp(LocalDateTime rawTimestamp) { this.rawTimestamp = rawTimestamp; }
    public Isin getIsin() { return isin; }
    public void setIsin(Isin isin) { this.isin = isin; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDepot() { return depot; }
    public void setDepot(String depot) { this.depot = depot; }
    public Double getValue() { return value; }
    public void setValue(Double value) { this.value = value; }

    public static DividendPaymentDtoBuilder builder() { return new DividendPaymentDtoBuilder(); }
}
