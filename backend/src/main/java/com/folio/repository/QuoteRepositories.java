package com.folio.repository;

import org.springframework.stereotype.Component;

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
        this.isinQuoteRepo = isinQuoteRepo;
        this.providerRepo = providerRepo;
        this.settingRepo = settingRepo;
    }

    public IsinQuoteRepository isinQuote() { return isinQuoteRepo; }
    public QuoteProviderRepository provider() { return providerRepo; }
    public SettingRepository setting() { return settingRepo; }
}

