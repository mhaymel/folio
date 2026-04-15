package com.folio.controller;

import static com.folio.precondition.Precondition.notNull;

import com.folio.repository.IsinRepository;
import com.folio.repository.QuoteRepositories;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

@Component
record YahooQuoteDataAccess(EntityManager entityManager, IsinRepository isinRepository, QuoteRepositories quoteRepositories) {
    YahooQuoteDataAccess {
        notNull(entityManager);
        notNull(isinRepository);
        notNull(quoteRepositories);
    }
}
