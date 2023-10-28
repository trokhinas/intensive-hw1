package ru.liga.rateprediction.core.dao;

import com.opencsv.bean.CsvToBeanBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.liga.rateprediction.core.CurrencyType;
import ru.liga.rateprediction.core.RatePrediction;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class CsvCBRFRatePredictionDao implements RatePredictionDao {
    private final Map<CurrencyType, List<RatePrediction>> csvCache = new HashMap<>();
    private final Object csvCacheLock = new Object();

    private final Map<CurrencyType, String> filePaths;
    private final CsvParserParams csvParserParams;

    @Override
    public List<RatePrediction> findAllByCurrencyType(CurrencyType currencyType) {
        return getCacheOrRead(currencyType);
    }

    @Override
    public Optional<RatePrediction> findByCurrencyTypeAndDate(CurrencyType currencyType, LocalDate localDate) {
        return getCacheOrRead(currencyType).stream()
                .filter(ratePrediction -> ratePrediction.getDate().equals(localDate))
                .findFirst();
    }

    @Override
    public List<RatePrediction> getFirstOrderByDateDesc(CurrencyType currencyType, int count) {
        return getCacheOrRead(currencyType).stream()
                .sorted(RatePrediction.BY_DATE_DESC)
                .limit(count)
                .collect(Collectors.toList());
    }

    private List<RatePrediction> getCacheOrRead(CurrencyType currencyType) {
        if (csvCache.get(currencyType) == null) {
            log.debug("Missing cache hit [1]");
            synchronized (csvCacheLock) {
                if (csvCache.get(currencyType) == null) {
                    log.debug("Missing cache hit [2]. Start actually reading file.");
                    csvCache.put(currencyType, readCsv(currencyType));
                } else {
                    log.debug("Cache hit!");
                }
            }
        } else {
            log.debug("Cache hit!");
        }

        return new ArrayList<>(csvCache.get(currencyType));
    }

    private List<RatePrediction> readCsv(CurrencyType currencyType) {
        final String path = filePaths.get(currencyType);
        if (path == null) {
            throw new IllegalArgumentException(String.format(
                    "Not found filepath with currency rates for [%s]", currencyType.getCode()
            ));
        }
        log.debug("Resolved filePath = {} for currency = {}", path, currencyType);

        try (final InputStream inputStream = getClass().getResourceAsStream(path)) {
            if (inputStream == null) {
                throw new IllegalArgumentException(String.format(
                        "Not found file at path = [%s]", path
                ));
            }

            return new CsvToBeanBuilder<RateCBRFCsvRow>(new InputStreamReader(inputStream))
                    .withSeparator(csvParserParams.getSeparator())
                    .withEscapeChar(csvParserParams.getEscapeCharacter())
                    .withQuoteChar(csvParserParams.getQuoteCharacter())
                    .withType(RateCBRFCsvRow.class)
                    .build()
                    .stream()
                    .map(this::fromRateCBRFCsvRow)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new IllegalStateException(String.format(
                    "IOException occurred while process CSV file [%s]", path
            ), e);
        }
    }

    private RatePrediction fromRateCBRFCsvRow(RateCBRFCsvRow rateCBRFCsvRow) {
        log.trace("Map row = {}", rateCBRFCsvRow);
        final BigDecimal nominal = new BigDecimal(rateCBRFCsvRow.getNominal().replaceAll(",", ""));
        final RatePrediction result = new RatePrediction(
                rateCBRFCsvRow.getDate(),
                rateCBRFCsvRow.getRate().divide(nominal, RoundingMode.HALF_UP)
        );
        log.trace("Mapped row = {}", result);
        return result;
    }
}
