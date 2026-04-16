package com.folio.service;

import com.folio.quote.IsinsQuoteLoader;
import com.folio.repository.QuoteRepositories;
import jakarta.persistence.EntityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Configuration
class QuoteServiceConfig {

    @Bean
    QuoteService quoteService(IsinsQuoteLoader quoteLoader, QuoteRepositories repos,
                              EntityManager entityManager, TransactionTemplate transactionTemplate) {
        return new QuoteService(
            quoteLoader,
            new QuoteDataAccess(repos, entityManager, transactionTemplate),
            new FetchState(new AtomicBoolean(false), new AtomicLong(0)));
    }
}

