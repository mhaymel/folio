package com.folio.service;

import com.folio.domain.Isin;
import com.folio.dto.DividendPaymentDto;
import com.folio.dto.DividendPaymentFilter;
import com.folio.model.CurrencyEntity;
import com.folio.model.DepotEntity;
import com.folio.model.DividendPaymentContext;
import com.folio.model.DividendPaymentEntity;
import com.folio.model.DividendPaymentEntityValues;
import com.folio.model.IsinEntity;
import com.folio.model.IsinNameEntity;
import com.folio.repository.CurrencyRepository;
import com.folio.repository.DepotRepository;
import com.folio.repository.DividendPaymentRepository;
import com.folio.repository.IsinNameRepository;
import com.folio.repository.IsinRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DividendPaymentServiceTest {

    @Autowired
    private DividendPaymentService dividendPaymentService;

    @Autowired
    private DividendPaymentRepository dividendPaymentRepo;

    @Autowired
    private IsinRepository isinRepo;

    @Autowired
    private IsinNameRepository isinNameRepo;

    @Autowired
    private DepotRepository depotRepo;

    @Autowired
    private CurrencyRepository currencyRepo;

    private DepotEntity depotDeGiro;
    private DepotEntity depotZero;
    private CurrencyEntity eur;

    @BeforeEach
    void setUp() {
        depotDeGiro = depotRepo.findByName("DeGiro").orElseThrow();
        depotZero = depotRepo.findByName("ZERO").orElseThrow();
        eur = currencyRepo.findByName("EUR").orElseThrow();
    }

    private IsinEntity createIsin(String code) {
        IsinEntity isin = new IsinEntity();
        isin.setIsin(code);
        return isinRepo.save(isin);
    }

    private void createIsinName(IsinEntity isin, String name) {
        IsinNameEntity isinName = new IsinNameEntity();
        isinName.setIsin(isin);
        isinName.setName(name);
        isinNameRepo.save(isinName);
    }

    private DividendPaymentEntity createPayment(IsinEntity isin, DepotEntity depot, double value, LocalDateTime timestamp) {
        DividendPaymentEntity dp = new DividendPaymentEntity(null,
                new com.folio.model.DividendPaymentContext(timestamp, isin, depot),
                new com.folio.model.DividendPaymentEntityValues(eur, value));
        return dividendPaymentRepo.save(dp);
    }

    @Test
    void shouldReturnEmptyListWhenNoPayments() {
        List<DividendPaymentDto> result = dividendPaymentService.getDividendPayments(DividendPaymentFilter.none());
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnAllPayments() {
        IsinEntity basf = createIsin("DE000BASF111");
        createIsinName(basf, "BASF SE");
        createPayment(basf, depotDeGiro, 34.0, LocalDateTime.of(2026, 3, 15, 10, 30));
        createPayment(basf, depotZero, 17.0, LocalDateTime.of(2026, 6, 15, 10, 30));

        List<DividendPaymentDto> result = dividendPaymentService.getDividendPayments(DividendPaymentFilter.none());

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getIsin()).isEqualTo(new Isin("DE000BASF111"));
        assertThat(result.get(0).getName()).isEqualTo("BASF SE");
        assertThat(result.get(0).getTimestamp()).isEqualTo("15.06.2026");
    }

    @Test
    void shouldFilterByIsin() {
        IsinEntity basf = createIsin("DE000BASF111");
        IsinEntity apple = createIsin("US0378331005");
        createPayment(basf, depotDeGiro, 34.0, LocalDateTime.of(2026, 3, 15, 10, 0));
        createPayment(apple, depotDeGiro, 10.0, LocalDateTime.of(2026, 3, 16, 10, 0));

        var filter = new DividendPaymentFilter("BASF", "", "", null, null);
        List<DividendPaymentDto> result = dividendPaymentService.getDividendPayments(filter);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIsin()).isEqualTo(new Isin("DE000BASF111"));
    }

    @Test
    void shouldFilterByDepot() {
        IsinEntity basf = createIsin("DE000BASF111");
        createPayment(basf, depotDeGiro, 34.0, LocalDateTime.of(2026, 3, 15, 10, 0));
        createPayment(basf, depotZero, 17.0, LocalDateTime.of(2026, 3, 16, 10, 0));

        var filter = new DividendPaymentFilter("", "", "DeGiro", null, null);
        List<DividendPaymentDto> result = dividendPaymentService.getDividendPayments(filter);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDepot()).isEqualTo("DeGiro");
    }

    @Test
    void shouldFilterByMultipleDepots() {
        IsinEntity basf = createIsin("DE000BASF111");
        createPayment(basf, depotDeGiro, 34.0, LocalDateTime.of(2026, 3, 15, 10, 0));
        createPayment(basf, depotZero, 17.0, LocalDateTime.of(2026, 3, 16, 10, 0));

        var filter = new DividendPaymentFilter("", "", "DeGiro,ZERO", null, null);
        List<DividendPaymentDto> result = dividendPaymentService.getDividendPayments(filter);

        assertThat(result).hasSize(2);
    }

    @Test
    void shouldFilterByName() {
        IsinEntity basf = createIsin("DE000BASF111");
        IsinEntity apple = createIsin("US0378331005");
        createIsinName(basf, "BASF SE");
        createIsinName(apple, "Apple Inc.");
        createPayment(basf, depotDeGiro, 34.0, LocalDateTime.of(2026, 3, 15, 10, 0));
        createPayment(apple, depotDeGiro, 10.0, LocalDateTime.of(2026, 3, 16, 10, 0));

        var filter = new DividendPaymentFilter("", "Apple", "", null, null);
        List<DividendPaymentDto> result = dividendPaymentService.getDividendPayments(filter);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Apple Inc.");
    }

    @Test
    void shouldFilterByDateRange() {
        IsinEntity basf = createIsin("DE000BASF111");
        createPayment(basf, depotDeGiro, 34.0, LocalDateTime.of(2026, 1, 15, 10, 0));
        createPayment(basf, depotDeGiro, 17.0, LocalDateTime.of(2026, 6, 15, 10, 0));
        createPayment(basf, depotDeGiro, 20.0, LocalDateTime.of(2026, 12, 15, 10, 0));

        var filter = new DividendPaymentFilter("", "", "",
                LocalDate.of(2026, 3, 1), LocalDate.of(2026, 9, 30));
        List<DividendPaymentDto> result = dividendPaymentService.getDividendPayments(filter);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getValue()).isEqualTo(17.0);
    }

    @Test
    void shouldReturnNullNameWhenNoIsinName() {
        IsinEntity basf = createIsin("DE000BASF111");
        createPayment(basf, depotDeGiro, 34.0, LocalDateTime.of(2026, 3, 15, 10, 0));

        List<DividendPaymentDto> result = dividendPaymentService.getDividendPayments(DividendPaymentFilter.none());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isNull();
    }
}