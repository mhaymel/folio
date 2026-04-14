package com.folio.controller;

import com.folio.repository.IsinRepository;
import com.folio.repository.QuoteRepositories;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

@Component
record YahooQuoteDataAccess(EntityManager em, IsinRepository isinRepository, QuoteRepositories quoteRepos) {
    YahooQuoteDataAccess {
        requireNonNull(em);
        requireNonNull(isinRepository);
        requireNonNull(quoteRepos);
    }
}

