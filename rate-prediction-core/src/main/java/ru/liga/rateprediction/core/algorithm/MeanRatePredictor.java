package ru.liga.rateprediction.core.algorithm;

import lombok.AllArgsConstructor;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import ru.liga.rateprediction.core.DateUtils;
import ru.liga.rateprediction.core.RatePrediction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * This class implements rate prediction algorithm based on mean value of previously predicted rates.
 */
class MeanRatePredictor implements RatePredictor {
    @Override
    public List<RatePrediction> predictRange(@NotNull List<RatePrediction> initialData,
                                             @NotNull LocalDate startDateInclusive,
                                             @NotNull LocalDate endDateInclusive) {
        Prediction prediction = doInitialPrediction(validateAndCopy(initialData));
        while (startDateInclusive.compareTo(prediction.getPrediction().getDate()) > 0) {
            prediction = nextPrediction(prediction);
        }

        final List<RatePrediction> predictions = new ArrayList<>();
        predictions.add(prediction.getPrediction());
        while (endDateInclusive.compareTo(prediction.getPrediction().getDate()) > 0) {
            prediction = nextPrediction(prediction);
            predictions.add(prediction.getPrediction());
        }

        return predictions;
    }

    @Override
    public RatePrediction predictSingle(@NotNull List<RatePrediction> initialData,
                                        @NotNull LocalDate predictionDate) {
        Prediction prediction = doInitialPrediction(validateAndCopy(initialData));
        while (predictionDate.compareTo(prediction.getPrediction().getDate()) > 0) {
            prediction = nextPrediction(prediction);
        }

        return prediction.getPrediction();
    }

    private List<RatePrediction> validateAndCopy(List<RatePrediction> initialData) {
        if (initialData == null || initialData.isEmpty()) {
            throw new IllegalArgumentException("No initial data provided!");
        }

        final Predicate<RatePrediction> isInvalidPrediction =
                ratePrediction -> !DateUtils.isLocalDateInPastOrPresent(ratePrediction.getDate());
        if (initialData.stream().anyMatch(isInvalidPrediction)) {
            throw new IllegalArgumentException("Invalid initial data provided!");
        }

        return new ArrayList<>(initialData);
    }

    private Prediction doInitialPrediction(Collection<RatePrediction> initialData) {
        // put all initial predictions in deque ordered by date ascending. this way
        // we would have at first element of deque prediction that must be excluded first
        // and new predictions would be added at the end of deque
        final Deque<RatePrediction> deque = initialData.stream()
                .sorted(RatePrediction.BY_DATE_ASC)
                .collect(Collectors.toCollection(ArrayDeque::new));

        return new Prediction(
                deque,
                nextRatePrediction(deque.getLast(), initialData)
        );
    }

    private Prediction nextPrediction(Prediction prediction) {
        final Deque<RatePrediction> usedData = new ArrayDeque<>(prediction.getUsedData());
        usedData.removeFirst();
        usedData.addLast(prediction.getPrediction());

        return new Prediction(
                usedData,
                nextRatePrediction(usedData.getLast(), usedData)
        );
    }

    private RatePrediction nextRatePrediction(RatePrediction latestPrediction,
                                              Collection<RatePrediction> previousPredictions) {
        final LocalDate nextDate = latestPrediction.getDate().plusDays(1);
        final BigDecimal nextRate = previousPredictions.stream()
                .map(RatePrediction::getRate)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(previousPredictions.size()), 4, RoundingMode.HALF_UP);

        return new RatePrediction(nextDate, nextRate);
    }

    /**
     * Local private static data class containing prediction info
     */
    @Value
    @AllArgsConstructor
    private static class Prediction {
        Deque<RatePrediction> usedData;

        RatePrediction prediction;
    }
}
