package com.folio.controller;

/**
 * Parameter object for stock list filtering.
 */
record StockFilter(String isin, String tickerSymbol, String name,
                   String depot, String country, String branch) {}

