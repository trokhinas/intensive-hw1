package ru.liga.rateprediction.cli.commands;

import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import ru.liga.rateprediction.core.CurrencyType;
import ru.liga.rateprediction.core.algorithm.RatePredictionAlgorithm;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class PredictionCliCommandParserTest {

    public static Stream<Arguments> commandVariants() {
        return Stream.<Arguments>builder()
                .add(Arguments.of("Help", HelpPredictionCliCommand.class))
                .add(Arguments.of("exit", ExitPredictionCliCommand.class))
                .add(Arguments.of("rate usd week", RatePredictionCliCommand.class))
                .add(Arguments.of("qwe usd week", InvalidPredictionCliCommand.class))
                .add(Arguments.of("rate qwe week", InvalidPredictionCliCommand.class))
                .add(Arguments.of("rate usd qwe", InvalidPredictionCliCommand.class))

                .build();
    }

    public static Stream<Arguments> rateVariants() {
        return Stream.<Arguments>builder()
                .add(Arguments.of("rate usd week", new RatePredictionCliCommand(
                        RatePredictionAlgorithm.MEAN,
                        CurrencyType.USD,
                        LocalDate.now().plusDays(1),
                        LocalDate.now().plusDays(7),
                        null
                )))
                .add(Arguments.of("rate try tomorrow", new RatePredictionCliCommand(
                        RatePredictionAlgorithm.MEAN,
                        CurrencyType.TRY,
                        LocalDate.now().plusDays(1),
                        null,
                        null
                )))
                .add(Arguments.of("rate eur tomorrow", new RatePredictionCliCommand(
                        RatePredictionAlgorithm.MEAN,
                        CurrencyType.EUR,
                        LocalDate.now().plusDays(1),
                        null,
                        null
                )))
                .build();
    }

    @ParameterizedTest
    @MethodSource("commandVariants")
    void parse_whenInput_thenReturnCommand(String command, Class<?> expectedClass) {
        //given
        final PredictionCliCommandParser predictionCliCommandParser = new PredictionCliCommandParser(null);

        //when
        final PredictionCliCommand actual = predictionCliCommandParser.parse(command);

        //then
        assertThat(actual).isInstanceOf(expectedClass);
    }

    @ParameterizedTest
    @MethodSource("rateVariants")
    void parse_whenCommandIsRate_thenParseRateCommand(String command, RatePredictionCliCommand expected) {
        //given
        final PredictionCliCommandParser predictionCliCommandParser = new PredictionCliCommandParser(null);

        //when
        final PredictionCliCommand actual = predictionCliCommandParser.parse(command);

        //then
        assertThat(actual).asInstanceOf(InstanceOfAssertFactories.type(RatePredictionCliCommand.class))
                .satisfies(ratePredictionCliCommand -> {
                    assertThat(ratePredictionCliCommand.getRatePredictionAlgorithm())
                            .isEqualTo(expected.getRatePredictionAlgorithm());
                    assertThat(ratePredictionCliCommand.getCurrencyType()).isEqualTo(expected.getCurrencyType());
                    assertThat(ratePredictionCliCommand.getStartDate()).isEqualTo(expected.getStartDate());
                    assertThat(ratePredictionCliCommand.getEndDate()).isEqualTo(expected.getEndDate());
                });
    }
}