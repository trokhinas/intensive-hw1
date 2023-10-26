package ru.liga.rateprediction.core;

import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;

/**
 * Prediction model that contains data about predicted rate at specific date.
 */
@Value
public class RatePrediction {
    public static final Comparator<RatePrediction> BY_DATE_ASC = Comparator.comparing(RatePrediction::getDate);

    /**
     * Date of rate
     */
    @NotNull
    LocalDate date;

    /**
     * Rate value
     */
    @NotNull
    BigDecimal rate;
}
