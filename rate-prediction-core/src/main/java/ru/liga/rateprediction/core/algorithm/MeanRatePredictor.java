package ru.liga.rateprediction.core.algorithm;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.liga.rateprediction.core.CurrencyType;
import ru.liga.rateprediction.core.DateUtils;
import ru.liga.rateprediction.core.RatePrediction;
import ru.liga.rateprediction.core.dao.RatePredictionDao;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * This class implements rate prediction algorithm based on mean value of previously predicted rates.
 */
@Slf4j
@RequiredArgsConstructor
public class MeanRatePredictor implements RatePredictor {
    private final RatePredictionDao ratePredictionDao;
    private final int depth;
    @Override
    public List<RatePrediction> predict(@NotNull CurrencyType currencyType,
                                        @NotNull LocalDate startDateInclusive,
                                        @Nullable LocalDate endDateInclusive) {
        log.info("Start prediction currency = {} in range = [{};{}]", currencyType, startDateInclusive, endDateInclusive);
        final List<RatePrediction> initialData = fetchInitialData(currencyType);
        Prediction prediction = doInitialPrediction(initialData);
        checkIsPredictionPossible(prediction, startDateInclusive);

        log.info("Start prediction from {} to {}", prediction.getPrediction().getDate(), startDateInclusive);
        while (startDateInclusive.isAfter(prediction.prediction.getDate())) {
            prediction = nextPrediction(prediction);
        }

        final List<RatePrediction> predictions = new ArrayList<>();
        predictions.add(prediction.getPrediction());
        if (endDateInclusive != null) {
            log.info("End date is not empty. Predict to fill range from {} to {}",
                    prediction.getPrediction().getDate(), endDateInclusive
            );
            while (endDateInclusive.isAfter(prediction.getPrediction().getDate())) {
                prediction = nextPrediction(prediction);
                predictions.add(prediction.getPrediction());
            }
        }
        return predictions;
    }

    private void checkIsPredictionPossible(Prediction prediction, LocalDate startDateInclusive) {
        log.debug("Check is possible to do prediction with initial prediction data as = {} and startDate for prediction = {}",
                prediction.getPrediction(), startDateInclusive);
        if (startDateInclusive.isBefore(prediction.getPrediction().getDate())) {
            throw new IllegalArgumentException(String.format(
                    "It is not possible to predict past or present date = [%s] with initial data in [%s]",
                    startDateInclusive, prediction.getPrediction().getDate()
            ));
        }
    }

    private List<RatePrediction> fetchInitialData(CurrencyType currencyType) {
        log.info("Fetch initial data with depth = {}", depth);
        final List<RatePrediction> initialData = ratePredictionDao.getFirstOrderByDateDesc(currencyType, depth);
        if (initialData == null || initialData.isEmpty()) {
            throw new IllegalArgumentException("No initial data provided!");
        }

        final Predicate<RatePrediction> isInvalidPrediction =
                ratePrediction -> !DateUtils.isLocalDateInPastOrPresent(ratePrediction.getDate());
        if (initialData.stream().anyMatch(isInvalidPrediction)) {
            throw new IllegalArgumentException("Invalid initial data provided!");
        }

        logPredictions(initialData);
        return initialData;
    }

    private Prediction doInitialPrediction(Collection<RatePrediction> initialData) {
        // put all initial predictions in deque ordered by date ascending. this way
        // we would have at first element of deque prediction that must be excluded first
        // and new predictions would be added at the end of deque
        logPredictions(initialData);
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

    private void logPredictions(Collection<RatePrediction> predictions) {
        if (log.isTraceEnabled()) {
            log.trace(
                    "Prediction data = {}",
                    predictions.stream()
                            .sorted(RatePrediction.BY_DATE_ASC)
                            .map(x -> String.format("%s - %s", x.getDate(), x.getRate()))
                            .collect(Collectors.joining("\n\t", "\n\t", ""))
            );
        }
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
