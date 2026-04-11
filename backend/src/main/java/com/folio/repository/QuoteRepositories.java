package com.folio.repository;

import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

/**
 * Bundles repositories needed by QuoteService to keep its constructor within the 3-parameter limit.
 */
@Component
public final class QuoteRepositories {

    private final IsinQuoteRepository isinQuoteRepo;
    private final QuoteProviderRepository providerRepo;
    private final SettingRepository settingRepo;

    public QuoteRepositories(IsinQuoteRepository isinQuoteRepo, QuoteProviderRepository providerRepo,
                             SettingRepository settingRepo) {
        this.isinQuoteRepo = requireNonNull(isinQuoteRepo);
        this.providerRepo = requireNonNull(providerRepo);
        this.settingRepo = requireNonNull(settingRepo);
    }

    public IsinQuoteRepository isinQuote() { return isinQuoteRepo; }
    public QuoteProviderRepository provider() { return providerRepo; }
    public SettingRepository setting() { return settingRepo; }
}

