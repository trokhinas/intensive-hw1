package ru.liga.rateprediction.core;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.liga.rateprediction.core.algorithm.RatePredictionAlgorithm;
import ru.liga.rateprediction.core.algorithm.RatePredictor;
import ru.liga.rateprediction.core.algorithm.RatePredictorFactory;
import ru.liga.rateprediction.core.datasource.PredictionDataSource;
import ru.liga.rateprediction.core.datasource.PredictionDataSourceFactory;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
public class RatePredictionFacade {
    // todo пока не придумал как избавиться от этой зависимости
    private static final int PREDICTION_DEPTH = 7;
    @NotNull
    private final RatePredictorFactory ratePredictorFactory;

    @NotNull
    private final PredictionDataSourceFactory predictionDataSourceFactory;

    public RatePredictionFacade() {
        this(new RatePredictorFactory(), new PredictionDataSourceFactory());
    }

    public List<RatePrediction> predictRate(@NotNull RatePredictionAlgorithm algorithm,
                                            @NotNull CurrencyType currencyType,
                                            @NotNull LocalDate startDateInclusive,
                                            @Nullable LocalDate endDateInclusive) {
        validateDates(startDateInclusive, endDateInclusive);
        final RatePredictor ratePredictor = ratePredictorFactory.create(algorithm);
        final PredictionDataSource predictionDataSource = predictionDataSourceFactory.create(currencyType);
        final List<RatePrediction> initialData = predictionDataSource.getData(PREDICTION_DEPTH);
        return ratePredictor.predict(initialData, startDateInclusive, endDateInclusive);
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
}
