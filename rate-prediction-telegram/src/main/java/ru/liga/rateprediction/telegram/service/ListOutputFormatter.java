package ru.liga.rateprediction.telegram.service;

import ru.liga.rateprediction.core.CurrencyType;
import ru.liga.rateprediction.core.RatePrediction;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class ListOutputFormatter implements OutputFormatter<String> {
    private static final DateTimeFormatter OUTPUT_DATE_FORMATTER = DateTimeFormatter
            .ofPattern("E dd.MM.yyyy")
            .withLocale(Locale.forLanguageTag("ru"));
    private static final NumberFormat OUTPUT_RATE_FORMATTER = new DecimalFormat(
            "00.00", new DecimalFormatSymbols(Locale.forLanguageTag("ru"))
    );

    @Override
    public String format(Map<CurrencyType, List<RatePrediction>> currencyTypeListMap) {
        final StringJoiner outputJoiner = new StringJoiner("\n\n");
        currencyTypeListMap.entrySet().stream()
                .map(entry -> {
                    final CurrencyType currencyType = entry.getKey();
                    final String rates = entry.getValue().stream()
                            .map(this::rateToString)
                            .collect(Collectors.joining("\n\t", "\n\t", ""));

                    return String.format("%s: %s", currencyType.getCode(), rates);
                })
                .forEach(outputJoiner::add);

        return outputJoiner.toString();
    }

    private String rateToString(RatePrediction ratePrediction) {
        return String.format(
                "%s: %s",
                OUTPUT_DATE_FORMATTER.format(ratePrediction.getDate()),
                OUTPUT_RATE_FORMATTER.format(ratePrediction.getRate())
        );
    }
}
