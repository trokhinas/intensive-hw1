package ru.liga.rateprediction.core.algorithm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.liga.rateprediction.core.CurrencyType;
import ru.liga.rateprediction.core.RatePrediction;
import ru.liga.rateprediction.core.algorithm.regression.LinearRegression;
import ru.liga.rateprediction.core.dao.RatePredictionDao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class LinearRegressionRatePredictor implements RatePredictor {
    private final RatePredictionDao ratePredictionDao;

    private final LinearRegression<LocalDate, BigDecimal> linearRegression;
    private final int depth;

    @Override
    public List<RatePrediction> predict(@NotNull CurrencyType currencyType,
                                        @NotNull LocalDate startDateInclusive,
                                        @Nullable LocalDate endDateInclusive) {
        log.info("Start prediction currency = {} in range = [{};{}]", currencyType, startDateInclusive, endDateInclusive);
        initRegression(currencyType);
        final List<RatePrediction> ratePredictions = new ArrayList<>();
        LocalDate currentDate = startDateInclusive;
        LocalDate endDate = endDateInclusive == null ? currentDate : endDateInclusive;

        while (currentDate.compareTo(endDate) <= 0) {
            log.debug("Predict for date = {}", currentDate);
            final BigDecimal rate = linearRegression.predict(currentDate);
            ratePredictions.add(new RatePrediction(currentDate, rate));
            currentDate = currentDate.plusDays(1);
        }
        return ratePredictions;
    }

    // todo а как в многопоточке?
    private void initRegression(CurrencyType currencyType) {
        final List<RatePrediction> initialData = ratePredictionDao.getFirstOrderByDateDesc(currencyType, depth)
                .stream()
                .sorted(RatePrediction.BY_DATE_ASC)
                .toList();
        if (initialData.size() != depth) {
            throw new IllegalArgumentException("Not enough data to make a prediction!");
        }

        final LocalDate[] dates = new LocalDate[depth];
        final BigDecimal[] rates = new BigDecimal[depth];
        for (int i = 0; i < initialData.size(); i++) {
            final RatePrediction ratePrediction = initialData.get(i);
            dates[i] = ratePrediction.getDate();
            rates[i] = ratePrediction.getRate();
        }
        log.debug("Init regression");
        log.trace("Dates array = {}", Arrays.toString(dates));
        log.trace("Rates array = {}", Arrays.toString(rates));
        linearRegression.init(dates, rates);
    }
}
