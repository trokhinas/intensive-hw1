package ru.liga.rateprediction.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import ru.liga.rateprediction.core.algorithm.RatePredictionAlgorithm;
import ru.liga.rateprediction.core.algorithm.RatePredictor;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class RatePredictionServiceImplTest {
    private final OffsetDateTime clockOffsetDateTime = OffsetDateTime.of(
            LocalDate.of(2023, 10, 25),
            LocalTime.of(0,0,0),
            ZoneOffset.UTC
    );

    private final Clock clock = Clock.fixed(clockOffsetDateTime.toInstant(), ZoneOffset.UTC.normalized());

    public static Stream<Arguments> invalidDateVariants() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(
                        // start date in past
                        LocalDate.of(2023, 10, 24), null
                ))
                .add(Arguments.of(
                        // start date in present
                        LocalDate.of(2023, 10, 25),
                        null
                ))
                .add(Arguments.of(
                        // end date is less than start
                        LocalDate.of(2023, 10, 26),
                        LocalDate.of(2023, 10, 25)
                ))
                .add(Arguments.of(
                        //both in future but wrong order
                        LocalDate.of(2023, 10, 28),
                        LocalDate.of(2023, 10, 27)
                ))
                .build();
    }

    public static Stream<Arguments> validDateVariants() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(
                        LocalDate.of(2023, 10, 26),
                        null
                ))
                .add(Arguments.of(
                        LocalDate.of(2023, 10, 26),
                        LocalDate.of(2023, 10, 26)
                ))
                .add(Arguments.of(
                        LocalDate.of(2023, 10, 26),
                        LocalDate.of(2023, 10, 27)

                ))
                .build();
    }

    @Test
    void predictRate_whenAlgIsNotFound_thenThrowIAE() {
        //given
        final RatePredictionAlgorithm algorithm = RatePredictionAlgorithm.MEAN;
        final RatePredictionService ratePredictionService = new RatePredictionServiceImpl(clock, Map.of());
        final CurrencyType currencyType = CurrencyType.EUR;
        final LocalDate startDate = LocalDate.now().plusDays(1);

        //when + then
        assertThatThrownBy(() -> ratePredictionService.predictRate(algorithm, currencyType, startDate, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @MethodSource("invalidDateVariants")
    void predictRate_whenDatesIsInvalid_thenThrowIAE(LocalDate startDate, LocalDate endDate) {
        //given
        final RatePredictionAlgorithm algorithm = RatePredictionAlgorithm.MEAN;
        final RatePredictionService ratePredictionService = new RatePredictionServiceImpl(clock, Map.of());
        final CurrencyType currencyType = CurrencyType.EUR;

        //when + then
        assertThatThrownBy(() -> ratePredictionService.predictRate(algorithm, currencyType, startDate, endDate))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @MethodSource("validDateVariants")
    void predictRate_whenValidDates_thenReturnPredictions(LocalDate startDate, LocalDate endDate) {
        //given
        final RatePredictionAlgorithm algorithm = RatePredictionAlgorithm.MEAN;
        final RatePredictor ratePredictor = Mockito.mock(RatePredictor.class);
        final RatePredictionService ratePredictionService = new RatePredictionServiceImpl(clock, Map.of(
                algorithm, ratePredictor
        ));
        final CurrencyType currencyType = CurrencyType.EUR;
        final List<RatePrediction> expected = List.of(
                new RatePrediction(startDate, BigDecimal.ONE)
        );
        Mockito.when(ratePredictor.predict(currencyType, startDate, endDate)).thenReturn(expected);


        //when
        final List<RatePrediction> actual = ratePredictionService.predictRate(algorithm, currencyType, startDate, endDate);

        //when
        assertThat(actual).isEqualTo(expected);
    }
}