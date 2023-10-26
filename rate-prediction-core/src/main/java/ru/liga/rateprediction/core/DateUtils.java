package ru.liga.rateprediction.core;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;

@UtilityClass
public class DateUtils {
    public static boolean isLocalDateInPastOrPresent(@NotNull LocalDate localDate) {
        return !localDate.isAfter(LocalDate.now());
    }
}
