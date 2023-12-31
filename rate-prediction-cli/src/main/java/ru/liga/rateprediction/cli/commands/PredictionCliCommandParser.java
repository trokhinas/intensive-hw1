package ru.liga.rateprediction.cli.commands;

import ru.liga.rateprediction.cli.CliCommand;
import ru.liga.rateprediction.cli.PredictionRange;
import ru.liga.rateprediction.core.CurrencyType;
import ru.liga.rateprediction.core.RatePredictionFacade;
import ru.liga.rateprediction.core.algorithm.RatePredictionAlgorithm;

import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Pattern;

public class PredictionCliCommandParser {
    private static final Pattern ARGS_DELIMITER = Pattern.compile("\\s");

    private final RatePredictionFacade ratePredictionFacade;

    public PredictionCliCommandParser(RatePredictionFacade ratePredictionFacade) {
        this.ratePredictionFacade = ratePredictionFacade;
    }

    public PredictionCliCommand parse(String userInput) {
        final String[] args = ARGS_DELIMITER.split(userInput);
        final Optional<CliCommand> predictionCommand = CliCommand.byCode(args[0]);
        if (predictionCommand.isEmpty()) {
            return new InvalidPredictionCliCommand(String.format(
                    "Invalid command = %s! Available commands = %s",
                    args[0],
                    Arrays.toString(CliCommand.values())
            ));
        }

        return parseCommand(predictionCommand.get(), args);
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

        final Optional<CurrencyType> currencyType = CurrencyType.byCode(args[1]);
        if (currencyType.isEmpty()) {
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
                currencyType.get(),
                dates.getStart(),
                dates.getEnd(),
                ratePredictionFacade
        );
    }
}
