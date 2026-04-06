package com.folio.online.yahoo;

import com.folio.domain.Isin;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

final class IsinTickerSearchIntegrationTest {

    private final IsinTickerSearch search = new IsinTickerSearch();

    @Test
    void resolvesAxa() {
        // FR0000120628 = AXA SA → CS.PA (Euronext Paris)
        IsinTickerSearchResult result = search.search(Set.of(new Isin("FR0000120628")));

        printResult(result);
        assertThat(result.found()).containsKey(new Isin("FR0000120628"));
    }

    @Test
    void resolvesHpInc() {
        // US40434L1052 = HP Inc. → HPQ (NYSE)
        IsinTickerSearchResult result = search.search(Set.of(new Isin("US40434L1052")));

        printResult(result);
        assertThat(result.found()).containsKey(new Isin("US40434L1052"));
    }

    @Test
    void resolvesBatch() {
        // ISINs from dividende.csv
        Set<Isin> isins = Set.of(
            new Isin("FR0000120628"),  // AXA SA
            new Isin("US40434L1052"),  // HP Inc.
            new Isin("US8808901081"),  // Ternium
            new Isin("US2810201077"),  // Edison International
            new Isin("GRS831003009")   // Piraeus Bank
        );

        IsinTickerSearchResult result = search.search(isins);

        printResult(result);
        assertThat(result.found()).isNotEmpty();
        System.out.println("Resolved: " + result.found().size() + "/" + isins.size());
    }

    @Test
    void returnsNotFoundForBogusIsin() {
        IsinTickerSearchResult result = search.search(Set.of(new Isin("XX0000000000")));

        printResult(result);
        assertThat(result.notFound()).containsExactly(new Isin("XX0000000000"));
    }

    private void printResult(IsinTickerSearchResult result) {
        result.found().forEach((isin, ticker) ->
                System.out.println(isin.value() + " → " + ticker.value()));
        result.notFound().forEach(isin ->
                System.out.println(isin.value() + " → NOT FOUND"));
    }
}
