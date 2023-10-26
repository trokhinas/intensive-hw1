package ru.liga.rateprediction.cli.commands;

import org.jetbrains.annotations.NotNull;

class InvalidPredictionCliCommand implements PredictionCliCommand {
    @NotNull
    private final String reason;

    public InvalidPredictionCliCommand(@NotNull String reason) {
        this.reason = reason;
    }

    @Override
    public void execute() {
        System.out.printf("Something wrong with your command: %s. Please try again.\n", reason);
    }
}
