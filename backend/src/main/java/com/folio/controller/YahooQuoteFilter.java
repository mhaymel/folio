package com.folio.controller;

/**
 * Parameter object for Yahoo quote list filtering.
 */
record YahooQuoteFilter(String isin, String name, String ticker) {}

