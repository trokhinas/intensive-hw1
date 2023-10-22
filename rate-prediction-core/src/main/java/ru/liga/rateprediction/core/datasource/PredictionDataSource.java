package ru.liga.rateprediction.core.datasource;

import ru.liga.rateprediction.core.RatePrediction;

import java.util.List;

public interface PredictionDataSource {
    List<RatePrediction> getData(int rowsCount);
}
