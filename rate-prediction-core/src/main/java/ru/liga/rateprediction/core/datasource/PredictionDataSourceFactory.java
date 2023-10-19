package ru.liga.rateprediction.core.datasource;

import ru.liga.rateprediction.core.CurrencyType;
import ru.liga.rateprediction.core.RatePrediction;
import ru.liga.rateprediction.core.datasource.files.csv.CsvToBeanReader;
import ru.liga.rateprediction.core.datasource.files.csv.RateCBRFCsvRow;

public class PredictionDataSourceFactory {
    private static final String HARDCODED_DATA_FOLDER = "/data/csv";

    public PredictionDataSource create(CurrencyType currencyType) {
        final String filepath = String.format("%s/%s.csv", HARDCODED_DATA_FOLDER, currencyType.getCode());
        return new CsvFileDataSource<>(
                CsvToBeanReader.openCSV(),
                RateCBRFCsvRow.class,
                rateCBRFCsvRow -> new RatePrediction(rateCBRFCsvRow.getDate(), rateCBRFCsvRow.getRate()),
                filepath
        );
    }
}
