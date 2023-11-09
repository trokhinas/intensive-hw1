package ru.liga.rateprediction.core.dao;

import ru.liga.rateprediction.core.CurrencyType;
import ru.liga.rateprediction.core.RatePrediction;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RatePredictionDao {
    List<RatePrediction> findAllByCurrencyType(CurrencyType currencyType);

    Optional<RatePrediction> findByCurrencyTypeAndDate(CurrencyType currencyType, LocalDate localDate);

    List<RatePrediction> getFirstOrderByDateDesc(CurrencyType currencyType, int count);
}
