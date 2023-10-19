package ru.liga.rateprediction.cli;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

@Getter
@RequiredArgsConstructor
public enum CliCommand {
    RATE("rate");

    private final String code;

    public static Optional<CliCommand> byCode(String code) {
        return Arrays.stream(values())
                .filter(x -> x.getCode().equalsIgnoreCase(code))
                .findFirst();
    }
}
