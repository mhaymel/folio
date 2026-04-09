package com.folio.quote;

record CachedRate(double rate, long timestamp) {

    CachedRate(double rate) {
        this(rate, System.currentTimeMillis());
    }

    boolean isExpired() {
        return System.currentTimeMillis() - timestamp > 3_600_000; // 1 hour
    }
}
