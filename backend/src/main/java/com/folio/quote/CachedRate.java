package com.folio.quote;

record CachedRate(double rate, long timestamp) {

    private static final long EXPIRATION_MILLIS = 3_600_000;

    CachedRate(double rate) {
        this(rate, System.currentTimeMillis());
    }

    boolean isExpired() {
        return System.currentTimeMillis() - timestamp > EXPIRATION_MILLIS;
    }
}
