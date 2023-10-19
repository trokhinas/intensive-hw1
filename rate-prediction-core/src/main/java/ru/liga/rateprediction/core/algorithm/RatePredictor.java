package ru.liga.rateprediction.core.algorithm;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.liga.rateprediction.core.RatePrediction;

import java.time.LocalDate;
import java.util.List;

/**
 * Interface represents currency rate prediction algorithm
 */
public interface RatePredictor {
    /**
     * Method predict currency rates for provided date range
     *
     * @param initialData        initial data for prediction, must not be null or empty
     * @param startDateInclusive start date inclusive, not null
     * @param endDateInclusive   end date inclusive, not null
     * @return {@link List} of currency rates prediction in desired date range
     */
    List<RatePrediction> predictRange(@NotNull List<RatePrediction> initialData,
                                      @NotNull LocalDate startDateInclusive,
                                      @NotNull LocalDate endDateInclusive);

    /**
     * Method predict currency rate for provided date
     *
     * @param initialData    initial data for prediction, must not be null or empty
     * @param predictionDate date for prediction
     * @return currency rate prediction for desired date
     */
    RatePrediction predictSingle(@NotNull List<RatePrediction> initialData,
                                 @NotNull LocalDate predictionDate);

    default List<RatePrediction> predict(@NotNull List<RatePrediction> initialData,
                                         @NotNull LocalDate startDateInclusive,
                                         @Nullable LocalDate endDateInclusive) {
        if (endDateInclusive == null) {
            return List.of(predictSingle(initialData, startDateInclusive));
        } else {
            return predictRange(initialData, startDateInclusive, endDateInclusive);
        }
    }
}
