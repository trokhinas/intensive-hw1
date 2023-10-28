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
public class MysticalRatePredictor implements RatePredictor {
    private final RatePredictionDao ratePredictionDao;
    private final Random random;
    private final Map<CurrencyType, YearRange> yearRangeMap = new HashMap<>();
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
            ratePredictions.add(predictMystic(currentDate, currencyType));
            currentDate = currentDate.plusDays(1);
        }
        return ratePredictions;
    }

    private RatePrediction predictMystic(LocalDate predictionDate, CurrencyType currencyType) {
        final YearRange yearRange = yearRangeMap.computeIfAbsent(currencyType, x -> computeYearRange(currencyType));
        RatePrediction result = null;
        for (int i = 0; i < yearRange.getCountOfYears() && result == null; i++) {
            final int randomYear = yearRange.getRandomYear(random);
            final LocalDate mysticDate = predictionDate.withYear(randomYear);
            log.debug("Try to predict mystic date = {}, attempt = {}", mysticDate, i);
            result = ratePredictionDao.findByCurrencyTypeAndDate(currencyType, predictionDate.withYear(randomYear))
                    .orElse(null);
        }

        if (result == null) {
            throw new IllegalArgumentException(String.format(
                    "Can't predict rate for currency type = %s and date = %s in %d attempts",
                    currencyType, predictionDate, yearRange.getCountOfYears()
            ));
        }
        return new RatePrediction(predictionDate, result.getRate());
    }

    private YearRange computeYearRange(CurrencyType currencyType) {
        log.debug("Calculate year range for currency type = {}", currencyType);
        final List<RatePrediction> ratePredictions = ratePredictionDao.findAllByCurrencyType(currencyType);
        if (ratePredictions.isEmpty()) {
            throw new IllegalArgumentException("Not found data for currency type = " + currencyType);
        }

        log.debug("Fetched {} rows", ratePredictions.size());
        Integer min = null;
        Integer max = null;
        for (RatePrediction ratePrediction : ratePredictions) {
            final int year = ratePrediction.getDate().getYear();
            if (min == null || year < min) {
                min = year;
            }
            if (max == null || year > max) {
                max = year;
            }
        }
        log.debug("Min year is {}", min);
        log.debug("Max year is {}", max);
        return new YearRange(min, max);
    }

    private static class YearRange {
        int yearFrom;
        int yearTo;

        public YearRange(int yearFrom, int yearTo) {
            this.yearFrom = yearFrom;
            this.yearTo = yearTo;
        }

        public int getRandomYear(Random random) {
            return random.nextInt(yearFrom, yearTo + 1);
        }

        public int getCountOfYears() {
            return yearTo - yearFrom + 1;
        }
    }
}
