package ru.liga.rateprediction.core.algorithm.regression;

public interface LinearRegression<X, Y> {
    Y predict(X knownValue);

    void init(X[] x, Y[] y);
}
