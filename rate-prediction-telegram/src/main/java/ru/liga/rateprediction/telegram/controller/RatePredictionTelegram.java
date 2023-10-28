package ru.liga.rateprediction.telegram.controller;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.liga.rateprediction.core.CurrencyType;
import ru.liga.rateprediction.core.RatePredictionService;
import ru.liga.rateprediction.core.RatePredictionServiceImpl;
import ru.liga.rateprediction.core.algorithm.*;
import ru.liga.rateprediction.core.algorithm.regression.LinearRegressionFromInternetAdapter;
import ru.liga.rateprediction.core.dao.CsvCBRFRatePredictionDao;
import ru.liga.rateprediction.core.dao.CsvParserParams;
import ru.liga.rateprediction.core.dao.RatePredictionDao;
import ru.liga.rateprediction.telegram.controller.commands.RateCommand;
import ru.liga.rateprediction.telegram.controller.commands.StartCommand;
import ru.liga.rateprediction.telegram.service.GraphOutputFormatterImpl;
import ru.liga.rateprediction.telegram.service.ListOutputFormatter;
import ru.liga.rateprediction.telegram.service.OutputFormatter;

import java.time.Clock;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
public class RatePredictionTelegram {
    private static final String BOT_TOKEN_ENV_NAME = "bot-token";
    private static final String BOT_USER_NAME_ENV_NAME = "bot-user-name";
    public static void start() {
        try {
            new TelegramBotsApi(DefaultBotSession.class).registerBot(ratePredictionBot());
        } catch (TelegramApiException e) {
            log.error("Can't start telegram bot, due: {}", e.getMessage(), e);
        }
    }

    private static TelegramLongPollingBot ratePredictionBot() {
        final String botToken = getEnv(BOT_TOKEN_ENV_NAME);
        final String botUserName = getEnv(BOT_USER_NAME_ENV_NAME);
        return new RatePredictionBot(botToken, botUserName, List.of(startCommand(), rateCommand()));
    }

    private static String getEnv(String env) {
        log.debug("Fetch env = {}", env);
        final String value = System.getenv(env);
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Please define environment variable " + env + "!");
        }

        return value;
    }

    private static BotCommand rateCommand() {
        return new RateCommand(ratePredictionService(), listFormatter(), graphFormatter());
    }

    private static BotCommand startCommand() {
        return new StartCommand();
    }

    private static OutputFormatter<String> listFormatter() {
        return new ListOutputFormatter();
    }

    private static OutputFormatter<byte[]> graphFormatter() {
        return new GraphOutputFormatterImpl();
    }

    private static RatePredictionService ratePredictionService() {
        final RatePredictionDao ratePredictionDao = ratePredictionDao();
        return new RatePredictionServiceImpl(
                Clock.offset(Clock.systemUTC(), Duration.ofHours(3)),
                Map.of(
                        RatePredictionAlgorithm.MEAN, new MeanRatePredictor(ratePredictionDao, 7),
                        RatePredictionAlgorithm.PREVIOUS, new PreviousYearRatePredictor(ratePredictionDao),
                        RatePredictionAlgorithm.MYSTICAL, new MysticalRatePredictor(
                                ratePredictionDao,
                                new Random(System.currentTimeMillis())
                        ),
                        RatePredictionAlgorithm.LINEAR_REGRESSION, new LinearRegressionRatePredictor(
                                ratePredictionDao,
                                new LinearRegressionFromInternetAdapter<>(),
                                30
                        )
                )
        );
    }

    private static RatePredictionDao ratePredictionDao() {
        return new CsvCBRFRatePredictionDao(
                Arrays.stream(CurrencyType.values()).collect(Collectors.toMap(
                        currencyType -> currencyType,
                        currencyType -> String.format("/data/csv/%s.csv", currencyType.getCode())
                )),
                CsvParserParams.builder().build()
        );
    }
}
