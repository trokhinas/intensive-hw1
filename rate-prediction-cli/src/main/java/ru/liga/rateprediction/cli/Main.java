package ru.liga.rateprediction.cli;

import org.apache.commons.cli.*;
import ru.liga.rateprediction.core.CurrencyType;
import ru.liga.rateprediction.core.MeanRatePredictorParams;
import ru.liga.rateprediction.core.RatePrediction;
import ru.liga.rateprediction.core.RatePredictionFacade;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Main {
    private static final Set<CurrencyType> SUPPORTED_CURRENCY_TYPES = EnumSet.of(
            CurrencyType.EUR,
            CurrencyType.TRY,
            CurrencyType.USD
    );
    private static final DateTimeFormatter CLI_DATE_FORMATTER = DateTimeFormatter
            .ofPattern("E dd.MM.yyyy")
            .withLocale(Locale.forLanguageTag("ru"));
    private static final NumberFormat CLI_RATE_FORMATTER = new DecimalFormat(
            "##.00", new DecimalFormatSymbols(Locale.forLanguageTag("ru"))
    );

    private static final RatePredictionFacade FACADE = new RatePredictionFacade();

    public static void main(String[] args) {
        if (args.length != 3) {
            throw new IllegalArgumentException(
                    "Command must contains exactly 3 parameters: prediction type, currency type, range. " +
                            "For example: \"rate TRY tomorrow\" or \"rate USD week\""
            );
        }

        final Optional<CliCommand> cliCommand = CliCommand.byCode(args[0]);
        if (cliCommand.isEmpty()) {
            System.out.printf(
                    "Unknown command = %s! Supported commands = %s",
                    args[0], Arrays.toString(CliCommand.values())
            );
            return;
        }
        final CurrencyType currencyType = validateCurrencyType(args[1]);
        final PredictionRange predictionRange = validatePredictionRange(args[2]);
        final PredictionRange.Dates dates = predictionRange.toDates();
        FACADE.predictMean(currencyType, dates.getStart(), dates.getEnd(), MeanRatePredictorParams.builder().build())
                .stream()
                .map(Main::predictionToString)
                .forEach(System.out::println);
    }

    private static String predictionToString(RatePrediction ratePrediction) {
        return String.format(
                "%s - %s",
                CLI_DATE_FORMATTER.format(ratePrediction.getDate()),
                CLI_RATE_FORMATTER.format(ratePrediction.getRate())
        );
    }

    private static CurrencyType validateCurrencyType(String abbreviation) {
        try {
            final CurrencyType currencyType = CurrencyType.byCode(abbreviation);
            if (!SUPPORTED_CURRENCY_TYPES.contains(currencyType)) {
                throw new IllegalArgumentException(String.format(
                        "Unsupported currency type = %s! Supported currency types = %s",
                        currencyType, SUPPORTED_CURRENCY_TYPES
                ));
            }
            return currencyType;
        } catch (EnumConstantNotPresentException e) {
            throw new IllegalArgumentException(String.format(
                    "Unknown currency type = %s! Supported currency types = %s",
                    abbreviation, SUPPORTED_CURRENCY_TYPES
            ));
        }
    }

    private static PredictionRange validatePredictionRange(String code) {
        try {
            return PredictionRange.byCode(code);
        } catch (EnumConstantNotPresentException e) {
            throw new IllegalArgumentException(String.format(
                    "Unknown prediction range = %s! Supported currency types = %s",
                    code, Arrays.toString(PredictionRange.values())
            ));
        }
    }
}
