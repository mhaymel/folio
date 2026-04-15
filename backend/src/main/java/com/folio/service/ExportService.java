package com.folio.service;

import com.folio.dto.ExportColumn;
import com.folio.dto.ExportRequest;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
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

    private <T> ResponseEntity<byte[]> buildCsv(List<T> data, List<ExportColumn<T>> columns,
                                                 String filenameBase) {
        var builder = new StringBuilder();
        builder.append('\uFEFF');
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) builder.append(';');
            builder.append(csvQuote(columns.get(i).header()));
        }
        builder.append("\r\n");
        for (T row : data) {
            for (int i = 0; i < columns.size(); i++) {
                if (i > 0) builder.append(';');
                Object cellValue = columns.get(i).accessor().apply(row);
                builder.append(csvQuote(formatForCsv(cellValue)));
            }
            builder.append("\r\n");
        }
        byte[] bytes = builder.toString().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filenameBase + ".csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .contentLength(bytes.length)
                .body(bytes);
    }

    private String formatForCsv(Object cellValue) {
        if (cellValue == null) return "";
        if (cellValue instanceof Number number) {
            synchronized (DE_NF) {
                return DE_NF.format(number.doubleValue());
            }
        }
        return cellValue.toString();
    }

    private String csvQuote(String value) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private <T> ResponseEntity<byte[]> buildExcel(List<T> data, List<ExportColumn<T>> columns,
                                                   String filenameBase) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(filenameBase);

            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            CellStyle numStyle = workbook.createCellStyle();
            DataFormat dataFormat = workbook.createDataFormat();
            numStyle.setDataFormat(dataFormat.getFormat("#,##0.00"));

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columns.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns.get(i).header());
                cell.setCellStyle(headerStyle);
            }

            for (int rowIndex = 0; rowIndex < data.size(); rowIndex++) {
                Row row = sheet.createRow(rowIndex + 1);
                T item = data.get(rowIndex);
                for (int colIndex = 0; colIndex < columns.size(); colIndex++) {
                    Cell cell = row.createCell(colIndex);
                    Object cellValue = columns.get(colIndex).accessor().apply(item);
                    if (cellValue instanceof Number number) {
                        cell.setCellValue(number.doubleValue());
                        cell.setCellStyle(numStyle);
                    } else {
                        cell.setCellValue(cellValue != null ? cellValue.toString() : "");
                    }
                }
            }

            for (int i = 0; i < columns.size(); i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            byte[] bytes = out.toByteArray();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filenameBase + ".xlsx\"")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .contentLength(bytes.length)
                    .body(bytes);
        } catch (IOException exception) {
            throw new RuntimeException("Failed to generate Excel export", exception);
        }
    }
}
