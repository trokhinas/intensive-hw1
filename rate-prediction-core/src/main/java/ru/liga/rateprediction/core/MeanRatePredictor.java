package ru.liga.rateprediction.core;

import lombok.AllArgsConstructor;
import lombok.Data;
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
        final Prediction prediction = doInitialPrediction();
        while (!prediction.getPrediction().getDate().equals(startDateInclusive)) {
            nextPrediction(prediction);
        }

        final List<RatePrediction> predictions = new ArrayList<>();
        predictions.add(prediction.getPrediction());
        while (!prediction.getPrediction().getDate().equals(endDateInclusive)) {
            nextPrediction(prediction);
            predictions.add(prediction.getPrediction());
        }

        return predictions;
    }

    @Override
    public RatePrediction predict(@NotNull LocalDate predictionDate) {
        final Prediction prediction = doInitialPrediction();
        while (predictionDate.compareTo(prediction.getPrediction().getDate()) > 0) {
            nextPrediction(prediction);
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

    private void nextPrediction(Prediction prediction) {
        // update data that would be used for prediction
        prediction.getUsedData().removeFirst();
        prediction.getUsedData().addLast(prediction.getPrediction());

        // do the actual prediction
        prediction.setPrediction(nextRatePrediction(
                prediction.getPrediction(),
                prediction.getUsedData()
        ));
    }

    private RatePrediction nextRatePrediction(RatePrediction latestPrediction,
                                              Collection<RatePrediction> previousPredictions) {
        return new RatePrediction(
                latestPrediction.getDate().plusDays(1),
                predictRate(previousPredictions)
        );
    }

    private BigDecimal predictRate(Collection<RatePrediction> predictions) {
        return predictions.stream()
                .map(RatePrediction::getRate).reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(predictions.size()), 4, RoundingMode.HALF_UP);
    }

    /**
     * Local private static data class containing prediction info
     */
    @Data
    @AllArgsConstructor
    private static class Prediction {
        private final Deque<RatePrediction> usedData;

        private RatePrediction prediction;
    }
}
