package ru.liga.rateprediction.core;

import lombok.AllArgsConstructor;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This class implements rate prediction algorithm based on mean value of previously predicted rates.
 */
class MeanRatePredictor implements RatePredictor {
    private final List<RatePrediction> initialData;

    public MeanRatePredictor(List<RatePrediction> initialData) {
        //do safe copy
        this.initialData = validateAndCopy(initialData);
    }

    private List<RatePrediction> validateAndCopy(List<RatePrediction> initialData) {
        if (initialData == null || initialData.isEmpty()) {
            throw new IllegalArgumentException("No initial data provided!");
        }

        if (initialData.stream().anyMatch(this::isInvalidPrediction)) {
            throw new IllegalArgumentException("Invalid initial data provided!");
        }

        return new ArrayList<>(initialData);
    }

    //TODO предиктор не будет работать корректно, если initialData находится, например, в будущем.
    //TODO но, кажется, что коду валидации тут не место, однако неясно куда его впихнуть
    private boolean isInvalidPrediction(RatePrediction ratePrediction) {
        return !DateUtils.isLocalDateInPastOrPresent(ratePrediction.getDate());
    }

    @Override
    public List<RatePrediction> predict(@NotNull LocalDate startDateInclusive,
                                        @NotNull LocalDate endDateInclusive) {
        Prediction prediction = doInitialPrediction();
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
    public RatePrediction predict(@NotNull LocalDate predictionDate) {
        Prediction prediction = doInitialPrediction();
        while (predictionDate.compareTo(prediction.getPrediction().getDate()) > 0) {
            prediction = nextPrediction(prediction);
        }

        return prediction.getPrediction();
    }

    private Prediction doInitialPrediction() {
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
