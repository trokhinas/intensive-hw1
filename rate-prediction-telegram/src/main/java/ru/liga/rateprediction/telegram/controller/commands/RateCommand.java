package ru.liga.rateprediction.telegram.controller.commands;

import lombok.Builder;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.liga.rateprediction.core.CurrencyType;
import ru.liga.rateprediction.core.RatePrediction;
import ru.liga.rateprediction.core.RatePredictionService;
import ru.liga.rateprediction.core.algorithm.RatePredictionAlgorithm;
import ru.liga.rateprediction.telegram.controller.commands.adapt.ImageTelegramMessageAdapterImpl;
import ru.liga.rateprediction.telegram.controller.commands.adapt.TelegramMessageAdapter;
import ru.liga.rateprediction.telegram.controller.commands.adapt.TextTelegramMessageAdapter;
import ru.liga.rateprediction.telegram.service.OutputFormatter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class RateCommand extends AbstractBotCommand {
    private static final Pattern CURRENCY_DELIMITER = Pattern.compile(",");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private final RatePredictionService ratePredictionService;
    private final OutputFormatter<String> listFormatter;
    private final OutputFormatter<byte[]> graphFormatter;

    public RateCommand(RatePredictionService ratePredictionService,
                       OutputFormatter<String> listFormatter,
                       OutputFormatter<byte[]> graphFormatter) {
        super("rate", "Прогнозирование курсов валют");
        this.ratePredictionService = ratePredictionService;
        this.listFormatter = listFormatter;
        this.graphFormatter = graphFormatter;
    }

    @Override
    protected TelegramMessageAdapter prepareResponse(User user, Chat chat, String[] args) {
        try {
            log.info("Handle rate command with args = {}", args);
            return handleInput(user, chat, args);
        } catch (Exception e) {
            log.warn("Exception: {}", e.getMessage(), e);
            return TextTelegramMessageAdapter.builder()
                    .chatId(chat.getId())
                    .text("Error occurred: " + e.getMessage())
                    .build();
        }
    }

    private TelegramMessageAdapter handleInput(User user, Chat chat, String[] args) {
        final RateCommandArgs rateCommandArgs = parseArgs(args);
        log.info("Parsed args = {}", rateCommandArgs);
        final Map<CurrencyType, List<RatePrediction>> predictions = rateCommandArgs.getCurrencyTypes()
                .stream()
                .collect(Collectors.toMap(
                        currencyType -> currencyType,
                        currencyType -> ratePredictionService.predictRate(
                                rateCommandArgs.getRatePredictionAlgorithm(),
                                currencyType,
                                rateCommandArgs.getDateRange().getStartDate(),
                                rateCommandArgs.getDateRange().getEndDate()
                        )
                ));

        return switch (rateCommandArgs.getOutputType()) {
            case LIST -> TextTelegramMessageAdapter.builder()
                    .chatId(chat.getId())
                    .text(listFormatter.format(predictions))
                    .build();
            case GRAPH -> ImageTelegramMessageAdapterImpl.builder()
                    .chatId(chat.getId())
                    .image(graphFormatter.format(predictions))
                    .filename("predictions.png")
                    .build();
        };
    }

    private RateCommandArgs parseArgs(String[] args) {
        final List<CurrencyType> currencyTypes = parseCurrencyTypes(args);
        final PeriodType periodType = parsePeriodType(args);
        final RateCommandArgs.DateRange dateRange = parseDateRange(periodType, args);
        final RatePredictionAlgorithm ratePredictionAlgorithm = parseAlgorithm(args);
        OutputType outputType = OutputType.LIST;
        if (periodType == PeriodType.PERIOD) {
            if (args.length < 7) {
                throw new IllegalArgumentException("Output type is not provided, but required for periodType = " + periodType);
            }

            final String str = args[6];
            outputType = switch (str) {
                case "list" -> OutputType.LIST;
                case "graph" -> OutputType.GRAPH;
                default -> {
                    throw new IllegalArgumentException("Invalid output type is provided: " + str);
                }
            };
        }

        return RateCommandArgs.builder()
                .currencyTypes(currencyTypes)
                .dateRange(dateRange)
                .ratePredictionAlgorithm(ratePredictionAlgorithm)
                .outputType(outputType)
                .build();
    }

    private List<CurrencyType> parseCurrencyTypes(String[] args) {
        if (args.length < 1) {
            throw new IllegalArgumentException("Currency type is not provided!");
        }

        try {
            return CURRENCY_DELIMITER.splitAsStream(args[0])
                    .map(CurrencyType::byCode)
                    .collect(Collectors.toList());
        } catch (EnumConstantNotPresentException e) {
            throw new IllegalArgumentException("Invalid currency type is provided: " + e.getMessage());
        }
    }

    private PeriodType parsePeriodType(String[] args) {
        if (args.length < 2) {
            throw new IllegalArgumentException("Period type is not provided!");
        }

        final String str = args[1];
        if (str.startsWith("-")) {
            return switch (str.substring(1)) {
                case "date" -> PeriodType.DATE;
                case "period" -> PeriodType.PERIOD;
                default -> {
                    throw new IllegalArgumentException("Invalid period type is provided: " + str);
                }

            };
        } else {
            throw new IllegalArgumentException("Invalid period type is provided: " + str);
        }
    }

    private RateCommandArgs.DateRange parseDateRange(PeriodType periodType, String[] args) {
        if (args.length < 3) {
            throw new IllegalArgumentException("Date range is not provided!");
        }

        final String str = args[2];
        final LocalDate tomorrow = LocalDate.now().plusDays(1);
        if (periodType == PeriodType.DATE) {
            if (str.equals("tomorrow")) {
                return RateCommandArgs.DateRange.builder().startDate(tomorrow).build();
            } else {
                try {
                    final LocalDate date = LocalDate.parse(str, DATE_FORMATTER);
                    return RateCommandArgs.DateRange.builder().startDate(date).build();
                } catch (DateTimeParseException e) {
                    log.warn("Can't parse date = {}, due: {}", str, e.getMessage(), e);
                    throw new IllegalArgumentException("Can't parse date: " + str);
                }
            }
        } else if (periodType == PeriodType.PERIOD) {
            if (str.equals("week")) {
                return RateCommandArgs.DateRange.builder()
                        .startDate(tomorrow)
                        .endDate(tomorrow.plusWeeks(1))
                        .build();
            } else if (str.equals("month")) {
                return RateCommandArgs.DateRange.builder()
                        .startDate(tomorrow)
                        .endDate(tomorrow.plusMonths(1))
                        .build();
            } else {
                throw new IllegalArgumentException("Invalid period provided: " + str);
            }
        } else {
            throw new IllegalArgumentException("Unknown period type: " + periodType);
        }
    }

    private RatePredictionAlgorithm parseAlgorithm(String[] args) {
        if (args.length < 5) {
            throw new IllegalArgumentException("Algorithm parameter is not provided!");
        }

        final String str = args[4];
        try {
            return RatePredictionAlgorithm.byCode(str);
        } catch (EnumConstantNotPresentException e) {
            throw new IllegalArgumentException("Unknown algorithm is provided: " + str);
        }
    }

    @Value
    @Builder
    private static class RateCommandArgs {
        List<CurrencyType> currencyTypes;
        DateRange dateRange;
        RatePredictionAlgorithm ratePredictionAlgorithm;
        OutputType outputType;

        @Value
        @Builder
        private static class DateRange {
            LocalDate startDate;
            LocalDate endDate;
        }
    }

    private enum OutputType {
        LIST,
        GRAPH
    }

    private enum PeriodType {
        DATE,
        PERIOD
    }
}
