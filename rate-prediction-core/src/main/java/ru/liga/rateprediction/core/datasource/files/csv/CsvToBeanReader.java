package ru.liga.rateprediction.core.datasource.files.csv;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * This interface provides generic way to read CSV file
 */
public interface CsvToBeanReader {
    /**
     * Method tries to read CSV file and parse it lines to appropriate java bean class. Provided {@link InputStream}
     * would be closed in any case: after successful processing or after throwing an exception.
     *
     * @param inputStream  input stream of CSV file to read, not null
     * @param readerParams CSV reader params such as delimiter, escape character, etc., not null
     * @param beanClass    expected bean class, not null
     * @param count        count of lines to read, must be positive. If file contains fewer lines than wanted, method return list of less size than expected
     * @param <T>          generic type of expected bean class
     * @return {@link List} of beans from CSV file.
     * @throws IllegalArgumentException if count was not positive
     * @throws IOException              if any IOException occurred
     */
    <T> List<T> readLines(@NotNull InputStream inputStream,
                          @NotNull CsvParserParams readerParams,
                          @NotNull Class<T> beanClass,
                          int count) throws IOException;

    static CsvToBeanReader openCSV() {
        return new CsvToBeanReaderOpenCSV();
    }
}
