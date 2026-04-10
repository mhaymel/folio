package com.folio.controller;

/**
 * Parameter object for ticker symbol list filtering.
 */
record TickerSymbolFilter(String isin, String tickerSymbol, String name) {}

