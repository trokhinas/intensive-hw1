package ru.liga.rateprediction.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.liga.rateprediction.core.algorithm.RatePredictionAlgorithm;

import java.time.LocalDate;
import java.util.List;

public interface RatePredictionService {
    List<RatePrediction> predictRate(@NotNull RatePredictionAlgorithm ratePredictionAlgorithm,
                                     @NotNull CurrencyType currencyType,
                                     @NotNull LocalDate startDateInclusive,
                                     @Nullable LocalDate endDateInclusive);
}
