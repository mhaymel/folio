package com.folio.service;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Groups the mutable fetch-progress state of {@link QuoteService}.
 */
record FetchState(AtomicBoolean inProgress, AtomicLong lastScheduledFetch) {}

