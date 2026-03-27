package com.folio.dto;
import java.util.List;
public final class DiversificationDto {
    private List<DiversificationEntry> entries;
    private Double totalInvested;
    public DiversificationDto() {}
    public DiversificationDto(List<DiversificationEntry> entries, Double totalInvested) {
        this.entries = entries;
        this.totalInvested = totalInvested;
    }
    public List<DiversificationEntry> getEntries() { return entries; }
    public void setEntries(List<DiversificationEntry> entries) { this.entries = entries; }
    public Double getTotalInvested() { return totalInvested; }
    public void setTotalInvested(Double totalInvested) { this.totalInvested = totalInvested; }
    public static DiversificationDtoBuilder builder() { return new DiversificationDtoBuilder(); }
}