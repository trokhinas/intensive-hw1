package ru.liga.rateprediction.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.liga.rateprediction.core.files.csv.CsvParserParams;
import ru.liga.rateprediction.core.files.csv.CsvToBeanReader;
import ru.liga.rateprediction.core.files.csv.RateCBRFCsvRow;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

//TODO пока также не очень ясно, как построить входную точку, открытую для расширений, но не раскрывающую лишних деталей реализации
public class RatePredictionFacade {
    private final CsvToBeanReader csvToBeanReader = CsvToBeanReader.openCSV();

    public List<RatePrediction> predictMean(@NotNull CurrencyType currencyType,
                                            @NotNull LocalDate startDateInclusive,
                                            @Nullable LocalDate endDateInclusive,
                                            @Nullable MeanRatePredictorParams predictorParams) {
        predictorParams = predictorParams == null ? MeanRatePredictorParams.builder().build() : predictorParams;
        validateDates(startDateInclusive, endDateInclusive);
        validateMeanPredictorParams(predictorParams);

        try {
            final List<RatePrediction> initialData = getInitialDataFromHardcodedFile(currencyType, predictorParams.getDepth());
            final MeanRatePredictor predictor = new MeanRatePredictor(initialData);
            if (endDateInclusive == null) {
                return List.of(predictor.predict(startDateInclusive));
            } else {
                return predictor.predict(startDateInclusive, endDateInclusive);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<RatePrediction> getInitialDataFromHardcodedFile(CurrencyType currencyType, int count) throws IOException {
        final String path = String.format("/data/csv/%s.csv", currencyType.getCode());
        final InputStream inputStream = getClass().getResourceAsStream(path);
        if (inputStream == null) {
            throw new IllegalStateException("Not found hardcoded file for path = " + path);
        }

        return csvToBeanReader.readLines(inputStream, CsvParserParams.builder().build(), RateCBRFCsvRow.class, count)
                .stream()
                .map(x -> new RatePrediction(x.getDate(), x.getRate()))
                .collect(Collectors.toList());
    }

    private void validateDates(LocalDate startDateInclusive, LocalDate endDateInclusive) {
        if (DateUtils.isLocalDateInPastOrPresent(startDateInclusive)) {
            throw new IllegalArgumentException(String.format(
                    "Start date = %s is not in future!", startDateInclusive
            ));
        }

        if (endDateInclusive != null && DateUtils.isLocalDateInPastOrPresent(endDateInclusive)) {
            throw new IllegalArgumentException(String.format(
                    "End date = %s is not in future!", endDateInclusive
            ));
        }
    }

    private void validateMeanPredictorParams(MeanRatePredictorParams meanRatePredictorParams) {
        if (meanRatePredictorParams.getDepth() <= 0) {
            throw new IllegalArgumentException(String.format(
                    "Depth = %s is not positive!", meanRatePredictorParams.getDepth()
            ));
        }
    }
}
