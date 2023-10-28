package ru.liga.rateprediction.core.dao;

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
    //todo неясно как распарсить числа типа 1,000 в 1000, поэтому строка
    @CsvBindByName(column = "nominal", required = true)
    private String nominal;

    @CsvDate("M/d/yyyy")
    @CsvBindByName(column = "data", required = true)
    private LocalDate date;

    @CsvBindByName(column = "curs", required = true)
    private BigDecimal rate;

    @CsvBindByName(column = "cdx")
    private String cdx;
}
