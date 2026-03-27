package com.folio.quote;

final class CachedRate {
    final double rate;
    final long timestamp;

    CachedRate(double rate) {
        this.rate = rate;
        this.timestamp = System.currentTimeMillis();
    }

    boolean isExpired() {
        return System.currentTimeMillis() - timestamp > 3_600_000; // 1 hour
    }
}

