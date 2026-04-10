package com.folio.service;

import com.folio.repository.QuoteRepositories;
import jakarta.persistence.EntityManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Groups the data-access dependencies of {@link QuoteService}.
 */
record QuoteDataAccess(QuoteRepositories repos, EntityManager em, TransactionTemplate tx) {}

