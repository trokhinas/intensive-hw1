package ru.liga.rateprediction.core.algorithm;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import ru.liga.rateprediction.core.CurrencyType;
import ru.liga.rateprediction.core.RatePrediction;
import ru.liga.rateprediction.core.dao.RatePredictionDao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MeanRatePredictorTest {
    private static final int DEFAULT_DEPTH = 7;

    public static Stream<Arguments> predictSingleVariants() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(
                        //single value
                        LocalDate.of(2023, 10, 16),
                        List.of(new RatePrediction(LocalDate.of(2023, 10, 15), BigDecimal.ONE)),
                        new RatePrediction(LocalDate.of(2023, 10, 16), BigDecimal.ONE)
                ))
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
                        List.of(
                                new RatePrediction(LocalDate.of(2023, 10, 15), BigDecimal.ONE)
                        ),
                        List.of(
                                new RatePrediction(LocalDate.of(2023, 10, 16), BigDecimal.ONE),
                                new RatePrediction(LocalDate.of(2023, 10, 17), BigDecimal.ONE),
                                new RatePrediction(LocalDate.of(2023, 10, 18), BigDecimal.ONE)
                        )

                ))
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
                        /*
                        13: (1 + 2 + 3) / 3 = 2
                        14: (2 + 3 + 2) / 3 = 2.3333
                        15: (3 + 2 + 2.3333) / 3 = 2.4444
                        16: (2 + 2.3333 + 2.4444) / 3 = 2.2592
                        17: (2.3333 + 2.4444 + 2.2592) / 3 = 2.3456
                        18: (2.4444 + 2.2592 + 2.3456) / 3 = 2.3497
                        * */
                        List.of(
                                new RatePrediction(LocalDate.of(2023, 10, 16), BigDecimal.valueOf(2.2592)),
                                new RatePrediction(LocalDate.of(2023, 10, 17), BigDecimal.valueOf(2.3456)),
                                new RatePrediction(LocalDate.of(2023, 10, 18), BigDecimal.valueOf(2.3497))
                        )
                ))
                .build();
    }

    public static Stream<Arguments> invalidStartDateVariants() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(
                        //start date is current date
                        LocalDate.of(2023, 10, 20),
                        List.of(
                                new RatePrediction(LocalDate.of(2023, 10, 17), BigDecimal.ONE),
                                new RatePrediction(LocalDate.of(2023, 10, 18), BigDecimal.ONE),
                                new RatePrediction(LocalDate.of(2023, 10, 19), BigDecimal.ONE),
                                new RatePrediction(LocalDate.of(2023, 10, 20), BigDecimal.ONE)
                        )
                ))
                .add(Arguments.of(
                        //start date is past date
                        LocalDate.of(2023, 10, 20),
                        List.of(
                                new RatePrediction(LocalDate.of(2023, 10, 17), BigDecimal.ONE),
                                new RatePrediction(LocalDate.of(2023, 10, 18), BigDecimal.ONE),
                                new RatePrediction(LocalDate.of(2023, 10, 19), BigDecimal.ONE),
                                new RatePrediction(LocalDate.of(2023, 10, 20), BigDecimal.ONE),
                                new RatePrediction(LocalDate.of(2023, 10, 21), BigDecimal.ONE)

                        )
                ))
                .build();
    }

    @Test
    void predict_whenNoDataIsProvided_thenThrowIAE() {
        //given
        final RatePredictionDao ratePredictionDao = Mockito.mock(RatePredictionDao.class);
        final RatePredictor ratePredictor = new MeanRatePredictor(ratePredictionDao, DEFAULT_DEPTH);
        final LocalDate startDate = LocalDate.now().plusDays(1);
        final CurrencyType currencyType = CurrencyType.EUR;
        Mockito.when(ratePredictionDao.getFirstOrderByDateDesc(currencyType, DEFAULT_DEPTH)).thenReturn(List.of());

        //when + then
        assertThatThrownBy(() -> ratePredictor.predict(currencyType, startDate, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void predict_whenInvalidDataIsProvided_thenThrowIAE() {
        //given
        final RatePredictionDao ratePredictionDao = Mockito.mock(RatePredictionDao.class);
        final RatePredictor ratePredictor = new MeanRatePredictor(ratePredictionDao, DEFAULT_DEPTH);
        final LocalDate startDate = LocalDate.now().plusDays(1);
        final LocalDate endDate = LocalDate.now().plusDays(7);
        final CurrencyType currencyType = CurrencyType.EUR;
        Mockito.when(ratePredictionDao.getFirstOrderByDateDesc(currencyType, DEFAULT_DEPTH)).thenReturn(List.of(
                new RatePrediction(LocalDate.now().minusDays(2), BigDecimal.ONE),
                new RatePrediction(LocalDate.now().minusDays(1), BigDecimal.TEN),
                new RatePrediction(LocalDate.now(), BigDecimal.ONE),
                new RatePrediction(LocalDate.now().plusDays(1), BigDecimal.ONE) // invalid, not in past or present
        ));

        //when + then
        assertThatThrownBy(() -> ratePredictor.predict(currencyType, startDate, endDate))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @MethodSource("invalidStartDateVariants")
    void predict_whenIsNotPossibleToPredictPastOrPresent_thenThrowIAE(LocalDate startDate,
                                                                      List<RatePrediction> initialData) {
        //given
        final RatePredictionDao ratePredictionDao = Mockito.mock(RatePredictionDao.class);
        final MeanRatePredictor ratePredictor = new MeanRatePredictor(ratePredictionDao, DEFAULT_DEPTH);
        final CurrencyType currencyType = CurrencyType.EUR;
        Mockito.when(ratePredictionDao.getFirstOrderByDateDesc(currencyType, DEFAULT_DEPTH)).thenReturn(initialData);

        //when + then
        assertThatThrownBy(() -> ratePredictor.predict(currencyType, startDate, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @MethodSource("predictSingleVariants")
    void predict_whenIsValidInputAndSingleDate_thenReturnSinglePrediction(LocalDate localDate,
                                                                          List<RatePrediction> initialData,
                                                                          RatePrediction expected) {
        //given
        final RatePredictionDao ratePredictionDao = Mockito.mock(RatePredictionDao.class);
        final MeanRatePredictor ratePredictor = new MeanRatePredictor(ratePredictionDao, DEFAULT_DEPTH);
        final CurrencyType currencyType = CurrencyType.EUR;
        Mockito.when(ratePredictionDao.getFirstOrderByDateDesc(currencyType, DEFAULT_DEPTH)).thenReturn(initialData);

        //when
        final List<RatePrediction> actual = ratePredictor.predict(currencyType, localDate, null);

        //then
        Assertions.assertThat(actual)
                .hasSize(1)
                .usingRecursiveFieldByFieldElementComparator(createRateComparisonConfiguration())
                .isEqualTo(List.of(expected));
    }

    @ParameterizedTest
    @MethodSource("predictRangeVariants")
    void predictRange_whenIsValidInputAndTwoDates_thenReturnListOfPredictions(LocalDate startDate,
                                                                              LocalDate endDate,
                                                                              List<RatePrediction> initialData,
                                                                              List<RatePrediction> expected) {
        //given
        final RatePredictionDao ratePredictionDao = Mockito.mock(RatePredictionDao.class);
        final MeanRatePredictor ratePredictor = new MeanRatePredictor(ratePredictionDao, DEFAULT_DEPTH);
        final CurrencyType currencyType = CurrencyType.EUR;
        Mockito.when(ratePredictionDao.getFirstOrderByDateDesc(currencyType, DEFAULT_DEPTH)).thenReturn(initialData);

        //when
        final List<RatePrediction> actual = ratePredictor.predict(CurrencyType.EUR, startDate, endDate);

        //then
        Assertions.assertThat(actual)
                .usingRecursiveFieldByFieldElementComparator(createRateComparisonConfiguration())
                .containsExactlyElementsOf(expected);
    }

    private RecursiveComparisonConfiguration createRateComparisonConfiguration() {
        final BigDecimal precision = BigDecimal.valueOf(0.00001);

        final RecursiveComparisonConfiguration configuration = new RecursiveComparisonConfiguration();
        configuration.registerComparatorForFields(
                // abs diff must be less than or equal to precision
                (BigDecimal a, BigDecimal b) -> a.subtract(b).abs().compareTo(precision) <= 0 ? 0 : 1,
                "rate"
        );

        return configuration;
    }
}