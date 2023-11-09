package ru.liga.rateprediction.cli.controller;

import ru.liga.rateprediction.cli.commands.PredictionCliCommand;
import ru.liga.rateprediction.cli.commands.PredictionCliCommandParser;
import ru.liga.rateprediction.core.CurrencyType;
import ru.liga.rateprediction.core.RatePredictionService;
import ru.liga.rateprediction.core.RatePredictionServiceImpl;
import ru.liga.rateprediction.core.algorithm.MeanRatePredictor;
import ru.liga.rateprediction.core.algorithm.RatePredictionAlgorithm;
import ru.liga.rateprediction.core.algorithm.RatePredictor;
import ru.liga.rateprediction.core.dao.CsvCBRFRatePredictionDao;
import ru.liga.rateprediction.core.dao.CsvParserParams;
import ru.liga.rateprediction.core.dao.RatePredictionDao;

import java.time.Clock;
import java.util.Arrays;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

public class RatePredictionCli {
    private static final int MEAN_PREDICTOR_DEPTH = 7;

    public static void start() {
        final Scanner scanner = new Scanner(System.in);
        final PredictionCliCommandParser predictionCliCommandParser = predictionCliCommandParser();

        predictionCliCommandParser.executeHelp();
        while (true) {
            System.out.print("Input your command: ");
            final String input = scanner.nextLine();

            final PredictionCliCommand command = predictionCliCommandParser.parse(input);
            try {
                command.execute();
            } catch (Exception e) {
                System.out.printf("Something went wrong during execution of \"%s\": %s\n", input, e.getMessage());
            }
        }
    }

    public static PredictionCliCommandParser predictionCliCommandParser() {
        return new PredictionCliCommandParser(ratePredictionService());
    }

    public static RatePredictionService ratePredictionService() {
        return new RatePredictionServiceImpl(Clock.systemUTC(), algorithmsMap());
    }

    private static Map<RatePredictionAlgorithm, RatePredictor> algorithmsMap() {
        return Map.of(
                RatePredictionAlgorithm.MEAN, new MeanRatePredictor(ratePredictionDao(), MEAN_PREDICTOR_DEPTH)
        );
    }

    private static RatePredictionDao ratePredictionDao() {
        return new CsvCBRFRatePredictionDao(csvFilePaths(), csvParserParams());
    }

    private static CsvParserParams csvParserParams() {
        return CsvParserParams.builder()
                .separator(';')
                .quoteCharacter('\"')
                .escapeCharacter('\\')
                .build();
    }

    private static Map<CurrencyType, String> csvFilePaths() {
        return Arrays.stream(CurrencyType.values()).collect(Collectors.toMap(
                currencyType -> currencyType,
                currencyType -> String.format("/data/csv/%s.csv", currencyType.getCode())
        ));
    }
}
