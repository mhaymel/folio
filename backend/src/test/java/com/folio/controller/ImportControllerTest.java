package com.folio.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
final class ImportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // branches.csv format: ISIN;Name;BranchEntity (no header row)
    @Test
    void importBranchesValidCsvReturnsSuccess() throws Exception {
        String csv = "DE000BASF111;BASF SE;Chemicals\nUS0378331005;Apple Inc.;Technology";
        MockMultipartFile file = new MockMultipartFile("file", "branches.csv", "text/csv", csv.getBytes());

        mockMvc.perform(multipart("/api/import/branches").file(file))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.imported", is(2)));
    }

    // countries.csv format: ISIN;Name;CountryEntity (no header row)
    @Test
    void importCountriesValidCsvReturnsSuccess() throws Exception {
        String csv = "DE000BASF111;BASF SE;Germany\nUS0378331005;Apple Inc.;USA";
        MockMultipartFile file = new MockMultipartFile("file", "countries.csv", "text/csv", csv.getBytes());

        mockMvc.perform(multipart("/api/import/countries").file(file))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.imported", is(2)));
    }

    // dividende.csv format: ISIN;Name;CurrencyEntity;DividendPerShare (no header row)
    @Test
    void importDividendsValidCsvReturnsSuccess() throws Exception {
        String csv = "DE000BASF111;BASF SE;EUR;3,40\nUS0378331005;Apple Inc.;USD;0,96";
        MockMultipartFile file = new MockMultipartFile("file", "dividende.csv", "text/csv", csv.getBytes());

        mockMvc.perform(multipart("/api/import/dividends").file(file))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.imported", is(2)));
    }

    // ticker_symbol.csv format: ISIN;TickerSymbolEntity;Name (no header row)
    @Test
    void importTickerSymbolsValidCsvReturnsSuccess() throws Exception {
        String csv = "DE000BASF111;BAS.DE;BASF SE\nUS0378331005;AAPL;Apple Inc.";
        MockMultipartFile file = new MockMultipartFile("file", "ticker_symbol.csv", "text/csv", csv.getBytes());

        mockMvc.perform(multipart("/api/import/ticker-symbols").file(file))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.imported", is(2)));
    }

    // ZERO orders: header + 23 semicolon-separated columns
    // Indices: 0=Name, 1=ISIN, 2=WKN, 3=Anzahl, 4=Anzahl storniert, 5=Status,
    //   6=Orderart, 7=Limit, 8=Stop, 9=Erstellt Datum, 10=Erstellt Zeit,
    //   11=Gültig bis, 12=Richtung, 13=Wert, 14=Wert storniert,
    //   15=Mindermengenzuschlag, 16=Ausführung Datum, 17=Ausführung Zeit,
    //   18=Ausführung Kurs, 19=Anzahl ausgeführt, 20=Anzahl offen,
    //   21=Gestrichen Datum, 22=Gestrichen Zeit

    private static final String ZERO_HEADER = "Name;ISIN;WKN;Anzahl;Anzahl storniert;Status;Orderart;Limit;Stop;"
        + "Erstellt Datum;Erstellt Zeit;Gültig bis;Richtung;Wert;Wert storniert;Mindermengenzuschlag;"
        + "Ausführung Datum;Ausführung Zeit;Ausführung Kurs;Anzahl ausgeführt;Anzahl offen;"
        + "Gestrichen Datum;Gestrichen Zeit";

    private static String zeroOrderRow(String name, String isin, String direction,
                                       String execPrice, String execCount) {
        // Fill required columns, leave others empty
        return name + ";" + isin + ";WKN123;1;;ausgeführt;Limit;;;01.01.2025;10:00:00;31.12.2025;"
            + direction + ";-100;;;01.01.2025;10:00:01;" + execPrice + ";" + execCount + ";0;;";
    }

    @Test
    void importZeroOrdersParsesGermanThousandsSeparator() throws Exception {
        // 1.000 in German = 1000 (dot is thousands separator)
        String csv = ZERO_HEADER + "\n"
            + zeroOrderRow("Test Stock", "DE000BASF111", "Kauf", "5,50", "1.000");
        MockMultipartFile file = new MockMultipartFile("file", "ZERO-orders.csv", "text/csv", csv.getBytes());

        mockMvc.perform(multipart("/api/import/zero/orders").file(file))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.imported", is(1)));
    }

    @Test
    void importZeroOrdersParsesGermanThousandsWithDecimal() throws Exception {
        // 1.000,50 in German = 1000.50
        String csv = ZERO_HEADER + "\n"
            + zeroOrderRow("Test Stock", "DE000BASF111", "Kauf", "12,75", "1.000,50");
        MockMultipartFile file = new MockMultipartFile("file", "ZERO-orders.csv", "text/csv", csv.getBytes());

        mockMvc.perform(multipart("/api/import/zero/orders").file(file))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.imported", is(1)));
    }

    @Test
    void importZeroOrdersParsesSimpleIntegerCount() throws Exception {
        String csv = ZERO_HEADER + "\n"
            + zeroOrderRow("Test Stock", "DE000BASF111", "Kauf", "29,53", "1");
        MockMultipartFile file = new MockMultipartFile("file", "ZERO-orders.csv", "text/csv", csv.getBytes());

        mockMvc.perform(multipart("/api/import/zero/orders").file(file))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.imported", is(1)));
    }

    @Test
    void importZeroOrdersSkipsNonExecutedRows() throws Exception {
        // Row with status "gestrichen" should be skipped
        String csv = ZERO_HEADER + "\n"
            + "Test;DE000BASF111;WKN;1;;gestrichen;Limit;;;01.01.2025;10:00:00;31.12.2025;Kauf;-100;;;;;;;0;;";
        MockMultipartFile file = new MockMultipartFile("file", "ZERO-orders.csv", "text/csv", csv.getBytes());

        mockMvc.perform(multipart("/api/import/zero/orders").file(file))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.imported", is(0)));
    }

    @Test
    void importBranchesEmptyCsvReturnsZeroImported() throws Exception {
        String csv = "";
        MockMultipartFile file = new MockMultipartFile("file", "branches.csv", "text/csv", csv.getBytes());

        mockMvc.perform(multipart("/api/import/branches").file(file))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.imported", is(0)));
    }

    @Test
    void importBranchesInvalidFormatReturnsSuccessWithZeroImported() throws Exception {
        // Only 2 fields per row, needs 3 — rows are silently skipped
        String csv = "DE000BASF111;Chemicals";
        MockMultipartFile file = new MockMultipartFile("file", "branches.csv", "text/csv", csv.getBytes());

        mockMvc.perform(multipart("/api/import/branches").file(file))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.imported", is(0)));
    }
}
