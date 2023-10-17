package ru.liga.rateprediction.core;

import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MeanRatePredictorTest {
    private static final BigDecimal PRECISION = BigDecimal.valueOf(0.00001);

    public static Stream<Arguments> predictSingleVariants() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(
                        //single value
                        LocalDate.of(2023, 10, 16),
                        List.of(new RatePrediction(LocalDate.of(2023, 10, 10), BigDecimal.ONE)),
                        new RatePrediction(LocalDate.of(2023, 10, 16), BigDecimal.ONE)
                ))
                .add(Arguments.of(
                        //single value
                        LocalDate.of(2023, 10, 22),
                        List.of(new RatePrediction(LocalDate.of(2023, 10, 10), BigDecimal.ONE)),
                        new RatePrediction(LocalDate.of(2023, 10, 22), BigDecimal.ONE)
                ))
                .add(Arguments.of(
                        LocalDate.of(2023, 10, 16),
                        /*
                        13: (1 + 2 + 3) / 3 = 2
                        14: (2 + 3 + 2) / 3 = 2.3333
                        15: (3 + 2 + 2.3333) / 3 = 2.4444
                        16: (2 + 2.3333 + 2.4444) / 3 = 2.2592
                        * */
                        List.of(
                                new RatePrediction(LocalDate.of(2023, 10, 10), BigDecimal.ONE),
                                new RatePrediction(LocalDate.of(2023, 10, 11), BigDecimal.valueOf(2)),
                                new RatePrediction(LocalDate.of(2023, 10, 12), BigDecimal.valueOf(3))
                        ),
                        new RatePrediction(LocalDate.of(2023, 10, 16), BigDecimal.valueOf(2.2592))
                ))
                .build();
    }

    public static Stream<Arguments> predictRangeVariants() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(
                        //single value
                        LocalDate.of(2023, 10, 16),
                        LocalDate.of(2023, 10, 18),
                        List.of(new RatePrediction(LocalDate.of(2023, 10, 10), BigDecimal.ONE)),
                        List.of(
                                new RatePrediction(LocalDate.of(2023, 10, 16), BigDecimal.ONE),
                                new RatePrediction(LocalDate.of(2023, 10, 17), BigDecimal.ONE),
                                new RatePrediction(LocalDate.of(2023, 10, 18), BigDecimal.ONE)
                        )

                ))
                .add(Arguments.of(
                        //single value
                        LocalDate.of(2023, 10, 22),
                        LocalDate.of(2023, 10, 25),
                        List.of(new RatePrediction(LocalDate.of(2023, 10, 10), BigDecimal.ONE)),
                        List.of(
                                new RatePrediction(LocalDate.of(2023, 10, 22), BigDecimal.ONE),
                                new RatePrediction(LocalDate.of(2023, 10, 23), BigDecimal.ONE),
                                new RatePrediction(LocalDate.of(2023, 10, 24), BigDecimal.ONE),
                                new RatePrediction(LocalDate.of(2023, 10, 25), BigDecimal.ONE)
                        )
                ))
                .add(Arguments.of(
                        LocalDate.of(2023, 10, 16),
                        LocalDate.of(2023, 10, 18),
                        List.of(
                                new RatePrediction(LocalDate.of(2023, 10, 10), BigDecimal.ONE),
                                new RatePrediction(LocalDate.of(2023, 10, 11), BigDecimal.valueOf(2)),
                                new RatePrediction(LocalDate.of(2023, 10, 12), BigDecimal.valueOf(3))
                        ),
                        List.of(
                                new RatePrediction(LocalDate.of(2023, 10, 16), BigDecimal.valueOf(2.2592)),
                                new RatePrediction(LocalDate.of(2023, 10, 17), BigDecimal.valueOf(2.3456)),
                                new RatePrediction(LocalDate.of(2023, 10, 18), BigDecimal.valueOf(2.3497))
                        )
                ))
                .build();
    }

    @Test
    void whenNoInitialDataIsPresent_thenThrow() {
        //given
        //no setup

        //when + then
        assertThatThrownBy(() -> new MeanRatePredictor(null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new MeanRatePredictor(Collections.emptyList())).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void whenInvalidInitialDataProvided_thenThrow() {
        //given
        final List<RatePrediction> invalidData = List.of(
                new RatePrediction(LocalDate.of(2022, 10, 16), BigDecimal.ONE),
                new RatePrediction(LocalDate.of(2023, 10, 15), BigDecimal.TEN),
                new RatePrediction(LocalDate.now(), BigDecimal.ONE),
                new RatePrediction(LocalDate.now().plusDays(1), BigDecimal.ONE) // invalid, not in past or present
        );

        //when + then
        assertThatThrownBy(() -> new MeanRatePredictor(invalidData)).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @MethodSource("predictSingleVariants")
    void predictSingle(LocalDate localDate, List<RatePrediction> initialData, RatePrediction expected) {
        //given
        final MeanRatePredictor ratePredictor = new MeanRatePredictor(initialData);

        //when
        final RatePrediction actual = ratePredictor.predict(localDate);

        //then
        assertThat(actual)
                .usingRecursiveComparison(createRateComparisonConfiguration())
                .isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource("predictRangeVariants")
    void predictRange(LocalDate startDate,
                      LocalDate endDate,
                      List<RatePrediction> initialData,
                      List<RatePrediction> expected) {
        //given
        final MeanRatePredictor ratePredictor = new MeanRatePredictor(initialData);

        //when
        final List<RatePrediction> actual = ratePredictor.predict(startDate, endDate);

        //then
        assertThat(actual)
                .usingRecursiveFieldByFieldElementComparator(createRateComparisonConfiguration())
                .containsExactlyElementsOf(expected);
    }

    private RecursiveComparisonConfiguration createRateComparisonConfiguration() {
        final RecursiveComparisonConfiguration configuration = new RecursiveComparisonConfiguration();
        configuration.registerComparatorForFields(
                // abs diff must be less than or equal to PRECISION
                (BigDecimal a, BigDecimal b) -> a.subtract(b).abs().compareTo(PRECISION) <= 0 ? 0 : 1,
                "rate"
        );

        return configuration;
    }
}