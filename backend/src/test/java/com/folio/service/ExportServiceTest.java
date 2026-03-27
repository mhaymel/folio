package com.folio.service;

import com.folio.dto.ExportRequest;
import com.folio.dto.ExportColumn;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

final class ExportServiceTest {

    private final ExportService exportService = new ExportService();

    @Test
    void shouldExportCsvWithHeaders() {
        // given
        record Row(String name, Double value) {}
        List<Row> data = List.of(new Row("Alpha", 10.5), new Row("Beta", 20.0));
        List<ExportColumn<Row>> columns = List.of(
                new ExportColumn<>("Name", Row::name),
                new ExportColumn<>("Value", Row::value)
        );

        // when
        ResponseEntity<byte[]> response = exportService.export(
                new ExportRequest<>(data, columns, "csv", "test"));

        // then
        String csv = new String(response.getBody());
        assertThat(csv).contains("\"Name\"");
        assertThat(csv).contains("\"Value\"");
        assertThat(csv).contains("\"Alpha\"");
        assertThat(response.getHeaders().getContentDisposition().getFilename()).isEqualTo("test.csv");
    }

    @Test
    void shouldExportExcelFormat() {
        // given
        record Row(String name) {}
        List<Row> data = List.of(new Row("Test"));
        List<ExportColumn<Row>> columns = List.of(new ExportColumn<>("Name", Row::name));

        // when
        ResponseEntity<byte[]> response = exportService.export(
                new ExportRequest<>(data, columns, "xlsx", "export"));

        // then
        assertThat(response.getBody()).isNotEmpty();
        assertThat(response.getHeaders().getContentDisposition().getFilename()).isEqualTo("export.xlsx");
    }

    @Test
    void shouldFormatNumbersInCsv() {
        // given
        record Row(Double price) {}
        List<Row> data = List.of(new Row(1234.5));
        List<ExportColumn<Row>> columns = List.of(new ExportColumn<>("Price", Row::price));

        // when
        ResponseEntity<byte[]> response = exportService.export(
                new ExportRequest<>(data, columns, "csv", "nums"));

        // then
        String csv = new String(response.getBody());
        // German locale: 1.234,50
        assertThat(csv).contains("1.234,50");
    }

    @Test
    void shouldHandleNullValuesInCsv() {
        // given
        record Row(String name) {}
        List<Row> data = List.of(new Row(null));
        List<ExportColumn<Row>> columns = List.of(new ExportColumn<>("Name", Row::name));

        // when
        ResponseEntity<byte[]> response = exportService.export(
                new ExportRequest<>(data, columns, "csv", "nulls"));

        // then
        String csv = new String(response.getBody());
        assertThat(csv).contains("\"\"");
    }

    @Test
    void shouldDefaultToCsvForUnknownFormat() {
        // given
        record Row(String name) {}
        List<Row> data = List.of(new Row("A"));
        List<ExportColumn<Row>> columns = List.of(new ExportColumn<>("Name", Row::name));

        // when
        ResponseEntity<byte[]> response = exportService.export(
                new ExportRequest<>(data, columns, "unknown", "test"));

        // then
        assertThat(response.getHeaders().getContentDisposition().getFilename()).isEqualTo("test.csv");
    }
}

