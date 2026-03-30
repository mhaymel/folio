package com.folio.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class DividendPaymentDtoBuilder {
    private static final DateTimeFormatter TIMESTAMP_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private Integer id;
    private LocalDateTime timestamp;
    private String isin;
    private String name;
    private String depot;
    private Double value;

    public DividendPaymentDtoBuilder id(Integer id) { this.id = id; return this; }
    public DividendPaymentDtoBuilder timestamp(LocalDateTime timestamp) { this.timestamp = timestamp; return this; }
    public DividendPaymentDtoBuilder isin(String isin) { this.isin = isin; return this; }
    public DividendPaymentDtoBuilder name(String name) { this.name = name; return this; }
    public DividendPaymentDtoBuilder depot(String depot) { this.depot = depot; return this; }
    public DividendPaymentDtoBuilder value(Double value) { this.value = value; return this; }

    public DividendPaymentDto build() {
        DividendPaymentDto dto = new DividendPaymentDto();
        dto.setId(id);
        dto.setTimestamp(timestamp != null ? timestamp.format(TIMESTAMP_FMT) : null);
        dto.setRawTimestamp(timestamp);
        dto.setIsin(isin);
        dto.setName(name);
        dto.setDepot(depot);
        dto.setValue(value);
        return dto;
    }
}