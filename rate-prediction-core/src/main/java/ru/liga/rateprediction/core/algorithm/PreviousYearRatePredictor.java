package ru.liga.rateprediction.core.algorithm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.liga.rateprediction.core.CurrencyType;
import ru.liga.rateprediction.core.RatePrediction;
import ru.liga.rateprediction.core.dao.RatePredictionDao;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
public class PreviousYearRatePredictor implements RatePredictor {
    private final RatePredictionDao ratePredictionDao;

    @Override
    public List<RatePrediction> predict(@NotNull CurrencyType currencyType,
                                        @NotNull LocalDate startDateInclusive,
                                        @Nullable LocalDate endDateInclusive) {
        log.info("Start prediction currency = {} in range = [{};{}]", currencyType, startDateInclusive, endDateInclusive);
        final List<RatePrediction> ratePredictions = new ArrayList<>();
        LocalDate currentDate = startDateInclusive;
        LocalDate endDate = endDateInclusive == null ? currentDate : endDateInclusive;

        while (currentDate.compareTo(endDate) <= 0) {
            log.debug("Predict for date = {}", currentDate);
            ratePredictions.add(predictPreviousYear(currentDate, currencyType));
            currentDate = currentDate.plusDays(1);
        }
        return ratePredictions;
    }

    private RatePrediction predictPreviousYear(LocalDate predictionDate, CurrencyType currencyType) {
        final LocalDate previousYear = predictionDate.minusYears(1);
        log.debug("Try predict previous year date = {}", previousYear);
        return ratePredictionDao.findByCurrencyTypeAndDate(currencyType, previousYear)
                .or(() -> {
                    final LocalDate previousYearPreviousDay = previousYear.minusDays(1);
                    log.debug("Not found previous year date, try predict minus 1 day = {}", previousYearPreviousDay);
                    return ratePredictionDao.findByCurrencyTypeAndDate(currencyType, previousYearPreviousDay);
                })
                .map(previousPrediction -> new RatePrediction(predictionDate, previousPrediction.getRate()))
                .orElseThrow(() -> new IllegalArgumentException(String.format(
                        "Not found prediction for currency = %s and date = %s in previous year",
                        currencyType, predictionDate
                )));
    }
}
