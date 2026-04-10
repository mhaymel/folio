package com.test;

import static java.util.Objects.requireNonNull;

record MappingRequest(String idType, String idValue) {
    MappingRequest {
        requireNonNull(idType);
        requireNonNull(idValue);
    }
}

