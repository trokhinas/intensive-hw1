package ru.liga.rateprediction.core;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.util.List;

/**
 * Interface represents currency rate prediction algorithm
 */
public interface RatePredictor {
    /**
     * Method predict currency rates for provided date range
     *
     * @param startDateInclusive start date inclusive, not null
     * @param endDateInclusive   end date inclusive, not null
     * @return {@link List} of currency rates prediction in desired date range
     */
    List<RatePrediction> predict(@NotNull LocalDate startDateInclusive, @NotNull LocalDate endDateInclusive);

    /**
     * Method predict currency rate for provided date
     *
     * @param predictionDate date for prediction
     * @return currency rate prediction for desired date
     */
    RatePrediction predict(@NotNull LocalDate predictionDate);
}
