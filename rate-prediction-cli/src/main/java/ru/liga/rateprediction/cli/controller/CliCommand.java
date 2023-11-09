package ru.liga.rateprediction.cli.controller;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum CliCommand {
    RATE("rate"),
    HELP("help"),
    EXIT("exit");

    private final String code;

    @NotNull
    public static CliCommand byCode(String code) {
        return Arrays.stream(values())
                .filter(x -> x.getCode().equalsIgnoreCase(code))
                .findFirst()
                .orElseThrow(() -> new EnumConstantNotPresentException(CliCommand.class, code));
    }
}
