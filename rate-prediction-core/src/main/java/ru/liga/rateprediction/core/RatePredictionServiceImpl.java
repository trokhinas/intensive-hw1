package ru.liga.rateprediction.core;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.liga.rateprediction.core.algorithm.RatePredictionAlgorithm;
import ru.liga.rateprediction.core.algorithm.RatePredictor;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class RatePredictionServiceImpl implements RatePredictionService {
    private final Clock clock;
    private final Map<RatePredictionAlgorithm, RatePredictor> algorithmRatePredictorMap;

    @Override
    public List<RatePrediction> predictRate(@NotNull RatePredictionAlgorithm ratePredictionAlgorithm,
                                            @NotNull CurrencyType currencyType,
                                            @NotNull LocalDate startDateInclusive,
                                            @Nullable LocalDate endDateInclusive) {
        log.info("Start rate prediction with alg = {}, currency = {}, range = [{};{}]",
                ratePredictionAlgorithm, currencyType, startDateInclusive, endDateInclusive
        );

        validateDates(startDateInclusive, endDateInclusive);
        final RatePredictor ratePredictor = resolveRatePredictor(ratePredictionAlgorithm);
        log.debug("Resolved alg implementation = {}", ratePredictor);
        return ratePredictor.predict(currencyType, startDateInclusive, endDateInclusive);
    }

    private RatePredictor resolveRatePredictor(RatePredictionAlgorithm ratePredictionAlgorithm) {
        final RatePredictor ratePredictor = algorithmRatePredictorMap.get(ratePredictionAlgorithm);
        if (ratePredictor == null) {
            throw new IllegalArgumentException(String.format(
                    "Not found algorithm implementation for [%s]", ratePredictionAlgorithm
            ));
        }

        return ratePredictor;
    }

    private void validateDates(LocalDate startDateInclusive, LocalDate endDateInclusive) {
        if (!startDateInclusive.isAfter(LocalDate.now(clock))) {
            throw new IllegalArgumentException(String.format(
                    "Start date = %s is not in future!", startDateInclusive
            ));
        }

        if (endDateInclusive != null && endDateInclusive.isBefore(startDateInclusive)) {
            throw new IllegalArgumentException(String.format(
                    "End date = %s is before start date = %s!", endDateInclusive, startDateInclusive
            ));
        }
    }
}
