package com.folio.service;

import com.folio.dto.ExportColumn;
import com.folio.dto.ExportRequest;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

/**
 * Generic export service that converts a list of objects into CSV or Excel bytes
 * and wraps the result in a downloadable {@link ResponseEntity}.
 *
 * <p>Column accessors should return <strong>raw values</strong>:
 * <ul>
 *   <li>{@code Number} (Double, Integer, …) — written as real numeric cells in Excel,
 *       formatted with German locale (2 dp) in CSV.</li>
 *   <li>{@code String} — written as text in both formats.</li>
 *   <li>{@code null} — written as empty cell / empty string.</li>
 * </ul>
 */
@Service
public final class ExportService {

    private static final NumberFormat DE_NF;
    static {
        DE_NF = NumberFormat.getInstance(Locale.GERMANY);
        DE_NF.setMinimumFractionDigits(2);
        DE_NF.setMaximumFractionDigits(2);
    }

    /**
     * Build a downloadable response for the given export request.
     */
    public <T> ResponseEntity<byte[]> export(ExportRequest<T> request) {
        if ("xlsx".equalsIgnoreCase(request.format())) {
            return buildExcel(request.data(), request.columns(), request.filenameBase());
        }
        return buildCsv(request.data(), request.columns(), request.filenameBase());
    }

    // ── CSV ─────────────────────────────────────────────────────────────────

    private <T> ResponseEntity<byte[]> buildCsv(List<T> data, List<ExportColumn<T>> columns,
                                                 String filenameBase) {
        var sb = new StringBuilder();
        sb.append('\uFEFF'); // BOM
        // Header
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) sb.append(';');
            sb.append(csvQuote(columns.get(i).header()));
        }
        sb.append("\r\n");
        // Rows
        for (T row : data) {
            for (int i = 0; i < columns.size(); i++) {
                if (i > 0) sb.append(';');
                Object val = columns.get(i).accessor().apply(row);
                sb.append(csvQuote(formatForCsv(val)));
            }
            sb.append("\r\n");
        }
        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filenameBase + ".csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .contentLength(bytes.length)
                .body(bytes);
    }

    private static String formatForCsv(Object val) {
        if (val == null) return "";
        if (val instanceof Number num) {
            synchronized (DE_NF) {
                return DE_NF.format(num.doubleValue());
            }
        }
        return val.toString();
    }

    private static String csvQuote(String value) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    // ── Excel ───────────────────────────────────────────────────────────────

    private <T> ResponseEntity<byte[]> buildExcel(List<T> data, List<ExportColumn<T>> columns,
                                                   String filenameBase) {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet(filenameBase);

            // Bold header style
            CellStyle headerStyle = wb.createCellStyle();
            Font font = wb.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            // Numeric cell style — 2 decimal places
            CellStyle numStyle = wb.createCellStyle();
            DataFormat df = wb.createDataFormat();
            numStyle.setDataFormat(df.getFormat("#,##0.00"));

            // Header row
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columns.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns.get(i).header());
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            for (int r = 0; r < data.size(); r++) {
                Row row = sheet.createRow(r + 1);
                T item = data.get(r);
                for (int c = 0; c < columns.size(); c++) {
                    Cell cell = row.createCell(c);
                    Object val = columns.get(c).accessor().apply(item);
                    if (val instanceof Number num) {
                        cell.setCellValue(num.doubleValue());
                        cell.setCellStyle(numStyle);
                    } else {
                        cell.setCellValue(val != null ? val.toString() : "");
                    }
                }
            }

            // Auto-size columns
            for (int i = 0; i < columns.size(); i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            byte[] bytes = out.toByteArray();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filenameBase + ".xlsx\"")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .contentLength(bytes.length)
                    .body(bytes);
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate Excel export", e);
        }
    }
}
