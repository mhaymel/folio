package com.folio.controller;

import com.folio.repository.IsinRepository;
import com.folio.repository.QuoteRepositories;
import jakarta.persistence.EntityManager;

/**
 * Groups data-access dependencies for {@link YahooQuotesController}.
 */
record YahooQuoteDataAccess(EntityManager em, IsinRepository isinRepository, QuoteRepositories quoteRepos) {}

