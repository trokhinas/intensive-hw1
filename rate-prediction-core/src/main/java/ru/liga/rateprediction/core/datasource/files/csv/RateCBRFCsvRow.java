package ru.liga.rateprediction.core.datasource.files.csv;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * CSV row representation of Central Bank of Russia data
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RateCBRFCsvRow {
    @CsvBindByName(column = "nominal")
    private Integer nominal;

    @CsvDate("M/d/yyyy")
    @CsvBindByName(column = "data", required = true)
    private LocalDate date;

    @CsvBindByName(column = "curs", required = true)
    private BigDecimal rate;

    @CsvBindByName(column = "cdx")
    private String cdx;
}
