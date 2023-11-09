package ru.liga.rateprediction.cli.commands;

import lombok.RequiredArgsConstructor;
import ru.liga.rateprediction.cli.controller.CliCommand;
import ru.liga.rateprediction.cli.controller.PredictionRange;
import ru.liga.rateprediction.core.CurrencyType;
import ru.liga.rateprediction.core.RatePredictionService;
import ru.liga.rateprediction.core.algorithm.RatePredictionAlgorithm;

import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public class PredictionCliCommandParser {
    private static final Pattern ARGS_DELIMITER = Pattern.compile("\\s");

    private final RatePredictionService ratePredictionService;

    public PredictionCliCommand parse(String userInput) {
        final String[] args = ARGS_DELIMITER.split(userInput);
        try {
            final CliCommand predictionCommand = CliCommand.byCode(args[0]);
            return parseCommand(predictionCommand, args);
        } catch (EnumConstantNotPresentException e) {
            return new InvalidPredictionCliCommand(String.format(
                    "Invalid command = %s! Available commands = %s",
                    args[0],
                    Arrays.toString(CliCommand.values())
            ));
        }
    }

    public void executeHelp() {
        new HelpPredictionCliCommand().execute();
    }

    //TODO сюда явно стоит прикрутить стратегию, но получается, что это стратегия, внутри команды...
    // не слишком ли чересчур
    private PredictionCliCommand parseCommand(CliCommand cliCommand, String[] args) {
        return switch (cliCommand) {
            case HELP -> new HelpPredictionCliCommand();
            case EXIT -> new ExitPredictionCliCommand();
            case RATE -> parseRateCommand(args);
        };
    }

    private PredictionCliCommand parseRateCommand(String[] args) {
        if (args.length != 3) {
            return new InvalidPredictionCliCommand(
                    "Command must contain exactly 3 parameters: prediction type, currency type, prediction range"
            );
        }

        final CurrencyType currencyType;
        try {
            currencyType = CurrencyType.byCode(args[1]);
        } catch (EnumConstantNotPresentException e) {
            return new InvalidPredictionCliCommand(String.format(
                    "Invalid currency type = %s! Supported currency types = %s",
                    args[1],
                    Arrays.toString(CurrencyType.values())
            ));
        }

        final Optional<PredictionRange> predictionRange = PredictionRange.byCode(args[2]);
        if (predictionRange.isEmpty()) {
            return new InvalidPredictionCliCommand(String.format(
                    "Invalid prediction range = %s! Available prediction ranges = %s",
                    args[2],
                    Arrays.toString(PredictionRange.values())
            ));
        }
        final PredictionRange.Dates dates = predictionRange.get().toDates();

        return new RatePredictionCliCommand(
                RatePredictionAlgorithm.MEAN,
                currencyType,
                dates.getStart(),
                dates.getEnd(),
                ratePredictionService
        );
    }
}
