package ru.liga.rateprediction.core.datasource;

import ru.liga.rateprediction.core.RatePrediction;
import ru.liga.rateprediction.core.datasource.files.csv.CsvParserParams;
import ru.liga.rateprediction.core.datasource.files.csv.CsvToBeanReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

class CsvFileDataSource<T> implements PredictionDataSource {
    private final CsvToBeanReader csvToBeanReader;

    private final CsvParserParams csvParserParams;

    private final Class<T> csvBeanType;

    private final Function<T, RatePrediction> csvBeanMapper;
    private final String filepath;

    public CsvFileDataSource(CsvToBeanReader csvToBeanReader,
                             CsvParserParams csvParserParams,
                             Class<T> csvBeanType,
                             Function<T, RatePrediction> csvBeanMapper,
                             String filepath) {
        this.csvToBeanReader = csvToBeanReader;
        this.csvParserParams = csvParserParams;
        this.csvBeanType = csvBeanType;
        this.csvBeanMapper = csvBeanMapper;
        this.filepath = filepath;
    }

    @Override
    public List<RatePrediction> getData(int rows) {
        try(final InputStream inputStream = getInputStream()) {
            return csvToBeanReader.readLines(inputStream, csvParserParams, csvBeanType, rows)
                    .stream()
                    .map(csvBeanMapper)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private InputStream getInputStream() {
        final InputStream inputStream = getClass().getResourceAsStream(filepath);
        if (inputStream == null) {
            throw new IllegalArgumentException("Not found file at path = " + filepath);
        }

        return inputStream;
    }
}
