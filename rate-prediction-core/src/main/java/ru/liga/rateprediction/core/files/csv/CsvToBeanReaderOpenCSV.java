package ru.liga.rateprediction.core.files.csv;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This class provides generic way to read CSV file via OpenCSV library
 */
class CsvToBeanReaderOpenCSV implements CsvToBeanReader {

    public <T> List<T> readLines(@NotNull InputStream inputStream,
                                 @NotNull CsvParserParams readerParams,
                                 @NotNull Class<T> beanClass,
                                 int count) throws IOException {
        try (inputStream) {
            checkIsPositive(count, "count");
            final CsvToBean<T> csvToBean = new CsvToBeanBuilder<T>(prepareReader(inputStream, readerParams))
                    .withType(beanClass)
                    .build();

            final Iterator<T> iterator = csvToBean.iterator();
            final List<T> result = new ArrayList<>();
            while (iterator.hasNext() && result.size() < count) {
                result.add(iterator.next());
            }

            return result;
        }
    }

    private CSVReader prepareReader(@NotNull InputStream inputStream,
                                    @NotNull CsvParserParams readerParams) {
        final CSVParser parser = new CSVParserBuilder()
                .withSeparator(readerParams.getSeparator())
                .withEscapeChar(readerParams.getEscapeCharacter())
                .withQuoteChar(readerParams.getQuoteCharacter())
                .build();

        return new CSVReaderBuilder(new InputStreamReader(inputStream)).withCSVParser(parser).build();
    }

    private void checkIsPositive(int arg, String name) {
        if (arg <= 0) {
            throw new IllegalArgumentException(String.format(
                    "%s must be positive, but was = %d", name, arg
            ));
        }
    }
}
