package ru.liga.rateprediction.telegram.service;

import ru.liga.rateprediction.core.CurrencyType;
import ru.liga.rateprediction.core.RatePrediction;

import java.util.List;
import java.util.Map;

public interface OutputFormatter<T> {
    T format(Map<CurrencyType, List<RatePrediction>> currencyTypeListMap);
}
