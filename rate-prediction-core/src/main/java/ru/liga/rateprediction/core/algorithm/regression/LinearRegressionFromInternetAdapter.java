package ru.liga.rateprediction.core.algorithm.regression;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;

public class LinearRegressionFromInternetAdapter<X, Y> implements LinearRegression<LocalDate, BigDecimal> {
    private LinearRegressionFromInternet linearRegressionFromInternet;

    @Override
    public BigDecimal predict(LocalDate knownValue) {
        if (linearRegressionFromInternet == null) {
            throw new IllegalArgumentException("Call predict method only after call init method with regression data!");
        }
        return unAdoptRate(linearRegressionFromInternet.predict(adoptDate(knownValue)));
    }

    @Override
    public void init(LocalDate[] x, BigDecimal[] y) {
        final double[] adoptedDates = Arrays.stream(x).mapToDouble(this::adoptDate).toArray();
        final double[] adoptedRates = Arrays.stream(y).mapToDouble(this::adoptRate).toArray();
        linearRegressionFromInternet = new LinearRegressionFromInternet(adoptedDates, adoptedRates);
    }

    public double adoptRate(BigDecimal bigDecimal) {
        return bigDecimal.doubleValue();
    }

    public BigDecimal unAdoptRate(double value) {
        return new BigDecimal(value);
    }

    public double adoptDate(LocalDate localDate) {
        return localDate.toEpochDay();
    }

    public LocalDate unAdoptDate(double value) {
        return LocalDate.ofEpochDay((long) value);
    }
}
