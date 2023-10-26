package ru.liga.rateprediction.core.datasource;

import lombok.extern.slf4j.Slf4j;
import ru.liga.rateprediction.core.CurrencyType;
import ru.liga.rateprediction.core.RatePrediction;
import ru.liga.rateprediction.core.datasource.files.csv.CsvParserParams;
import ru.liga.rateprediction.core.datasource.files.csv.CsvToBeanReader;
import ru.liga.rateprediction.core.datasource.files.csv.RateCBRFCsvRow;

@Slf4j
public class PredictionDataSourceFactory {
    private static final String HARDCODED_DATA_FOLDER = "/data/csv";

    private static final CsvParserParams HARDCODED_PARAMS = CsvParserParams.builder()
            .separator(';')
            .quoteCharacter('\"')
            .escapeCharacter('\\')
            .build();

    public PredictionDataSource create(CurrencyType currencyType) {
        log.info("Create initial data source for currency = {}", currencyType);
        final String filepath = String.format("%s/%s.csv", HARDCODED_DATA_FOLDER, currencyType.getCode());
        log.debug("Hardcoded file path is = {}", filepath);

        return new CsvFileDataSource<>(
                CsvToBeanReader.openCSV(),
                HARDCODED_PARAMS,
                RateCBRFCsvRow.class,
                rateCBRFCsvRow -> new RatePrediction(rateCBRFCsvRow.getDate(), rateCBRFCsvRow.getRate()),
                filepath
        );
    }
}
