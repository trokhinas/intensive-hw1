package ru.liga.rateprediction.cli.commands;

import lombok.Getter;
import ru.liga.rateprediction.core.CurrencyType;
import ru.liga.rateprediction.core.RatePrediction;
import ru.liga.rateprediction.core.RatePredictionFacade;
import ru.liga.rateprediction.core.algorithm.RatePredictionAlgorithm;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Getter
class RatePredictionCliCommand implements PredictionCliCommand {
    private static final DateTimeFormatter CLI_DATE_FORMATTER = DateTimeFormatter
            .ofPattern("E dd.MM.yyyy")
            .withLocale(Locale.forLanguageTag("ru"));
    private static final NumberFormat CLI_RATE_FORMATTER = new DecimalFormat(
            "##.00", new DecimalFormatSymbols(Locale.forLanguageTag("ru"))
    );
    private final RatePredictionAlgorithm ratePredictionAlgorithm;
    private final CurrencyType currencyType;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final RatePredictionFacade ratePredictionFacade;

    public RatePredictionCliCommand(RatePredictionAlgorithm ratePredictionAlgorithm,
                                    CurrencyType currencyType,
                                    LocalDate startDate,
                                    LocalDate endDate,
                                    RatePredictionFacade ratePredictionFacade) {
        this.ratePredictionAlgorithm = ratePredictionAlgorithm;
        this.currencyType = currencyType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.ratePredictionFacade = ratePredictionFacade;
    }

    @Override
    public void execute() {
        ratePredictionFacade.predictRate(ratePredictionAlgorithm, currencyType, startDate, endDate)
                .stream()
                .map(this::predictionToString)
                .forEach(System.out::println);
    }

    private String predictionToString(RatePrediction ratePrediction) {
        return String.format(
                "%s - %s",
                CLI_DATE_FORMATTER.format(ratePrediction.getDate()),
                CLI_RATE_FORMATTER.format(ratePrediction.getRate())
        );
    }
}
