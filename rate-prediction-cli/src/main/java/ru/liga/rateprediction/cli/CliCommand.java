package ru.liga.rateprediction.cli;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Optional;

@Getter
@RequiredArgsConstructor
public enum CliCommand {
    RATE("rate"),
    HELP("help"),
    EXIT("exit");

    private final String code;

    @NotNull
    public static Optional<CliCommand> byCode(String code) {
        return Arrays.stream(values())
                .filter(x -> x.getCode().equalsIgnoreCase(code))
                .findFirst();
    }
}
