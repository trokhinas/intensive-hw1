package ru.liga.rateprediction.cli;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

@Getter
@RequiredArgsConstructor
public enum PredictionRange {
    TOMORROW("TOMORROW"),
    WEEK("WEEK");

    private final String code;

    public static Optional<PredictionRange> byCode(String code) {
        return Arrays.stream(values())
                .filter(x -> x.code.equalsIgnoreCase(code))
                .findFirst();
    }

    public PredictionRange.Dates toDates() {
        return switch (this) {
            case TOMORROW -> Dates.builder().start(LocalDate.now().plusDays(1)).build();
            case WEEK -> Dates.builder().start(LocalDate.now().plusDays(1)).end(LocalDate.now().plusDays(7)).build();
        };
    }

    @Value
    @Builder
    public static class Dates {
        @NotNull
        LocalDate start;

        @Nullable
        LocalDate end;
    }
}
