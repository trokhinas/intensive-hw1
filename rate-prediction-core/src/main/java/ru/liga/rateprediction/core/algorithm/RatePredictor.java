package ru.liga.rateprediction.core.algorithm;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.liga.rateprediction.core.CurrencyType;
import ru.liga.rateprediction.core.RatePrediction;

import java.time.LocalDate;
import java.util.List;

public interface RatePredictor {
    List<RatePrediction> predict(@NotNull CurrencyType currencyType,
                                 @NotNull LocalDate startDateInclusive,
                                 @Nullable LocalDate endDateInclusive);
}
