package ru.liga.rateprediction.core.algorithm;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import ru.liga.rateprediction.core.CurrencyType;
import ru.liga.rateprediction.core.RatePrediction;
import ru.liga.rateprediction.core.algorithm.regression.LinearRegression;
import ru.liga.rateprediction.core.dao.RatePredictionDao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class LinearRegressionRatePredictorTest {
    private static final int DEFAULT_DEPTH = 3;

    public static Stream<Arguments> wrongDataVariants() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(List.of()))
                .add(Arguments.of(List.of(
                        //less than
                        new RatePrediction(LocalDate.now(), BigDecimal.ONE)
                )))
                .add(Arguments.of(List.of(
                        //less than
                        new RatePrediction(LocalDate.now(), BigDecimal.ONE),
                        new RatePrediction(LocalDate.now(), BigDecimal.ONE)
                )))
                .add(Arguments.of(List.of(
                        //greater than
                        new RatePrediction(LocalDate.now(), BigDecimal.ONE),
                        new RatePrediction(LocalDate.now(), BigDecimal.ONE),
                        new RatePrediction(LocalDate.now(), BigDecimal.ONE),
                        new RatePrediction(LocalDate.now(), BigDecimal.ONE)
                )))
                .build();
    }

    @Test
    void predictSingle_whenDataIsProvided_thenPredict() {
        //given
        final LocalDate predictionDate = LocalDate.of(2023, 11, 6);
        final CurrencyType currencyType = CurrencyType.EUR;
        final List<RatePrediction> mockedData = List.of(
                new RatePrediction(predictionDate.minusDays(1), BigDecimal.valueOf(2)),
                new RatePrediction(predictionDate.minusDays(2), BigDecimal.valueOf(4)),
                new RatePrediction(predictionDate.minusDays(3), BigDecimal.valueOf(6))
        );
        final RatePrediction expected = new RatePrediction(predictionDate, BigDecimal.ONE);

        final RatePredictionDao ratePredictionDao = Mockito.mock(RatePredictionDao.class);
        final LinearRegression<LocalDate, BigDecimal> linearRegression = Mockito.mock(LinearRegression.class);
        final RatePredictor ratePredictor = new LinearRegressionRatePredictor(ratePredictionDao, linearRegression, DEFAULT_DEPTH);
        Mockito.when(ratePredictionDao.getFirstOrderByDateDesc(currencyType, DEFAULT_DEPTH)).thenReturn(mockedData);
        Mockito.when(linearRegression.predict(predictionDate)).thenReturn(BigDecimal.ONE);

        //when
        final List<RatePrediction> actual = ratePredictor.predict(currencyType, predictionDate, null);

        //then
        assertThat(actual).containsExactly(expected);
    }

    @Test
    void predictRange_whenDataIsProvided_thenReturnPredictions() {
        //given
        final LocalDate predictionDateStart = LocalDate.of(2023, 11, 6);
        final LocalDate predictionDateEnd = LocalDate.of(2023, 11, 8);
        final CurrencyType currencyType = CurrencyType.EUR;
        final List<RatePrediction> mockedData = List.of(
                new RatePrediction(predictionDateStart.minusDays(1), BigDecimal.valueOf(2)),
                new RatePrediction(predictionDateStart.minusDays(2), BigDecimal.valueOf(4)),
                new RatePrediction(predictionDateStart.minusDays(3), BigDecimal.valueOf(6))
        );
        final List<RatePrediction> expected = List.of(
                new RatePrediction(predictionDateStart, BigDecimal.ONE),
                new RatePrediction(predictionDateStart.plusDays(1), BigDecimal.valueOf(0.5)),
                new RatePrediction(predictionDateEnd, BigDecimal.valueOf(0.25))
        );

        final RatePredictionDao ratePredictionDao = Mockito.mock(RatePredictionDao.class);
        final LinearRegression<LocalDate, BigDecimal> linearRegression = Mockito.mock(LinearRegression.class);
        final RatePredictor ratePredictor = new LinearRegressionRatePredictor(ratePredictionDao, linearRegression, DEFAULT_DEPTH);
        Mockito.when(ratePredictionDao.getFirstOrderByDateDesc(currencyType, DEFAULT_DEPTH)).thenReturn(mockedData);
        Mockito.when(linearRegression.predict(predictionDateStart)).thenReturn(BigDecimal.ONE);
        Mockito.when(linearRegression.predict(predictionDateStart.plusDays(1))).thenReturn(BigDecimal.valueOf(0.5));
        Mockito.when(linearRegression.predict(predictionDateEnd)).thenReturn(BigDecimal.valueOf(0.25));

        //when
        final List<RatePrediction> actual = ratePredictor.predict(currencyType, predictionDateStart, predictionDateEnd);

        //then
        assertThat(actual).containsExactlyElementsOf(expected);
    }

    @ParameterizedTest
    @MethodSource("wrongDataVariants")
    void predictSingle_whenNotEnoughData_thenThrowIAE() {
        //given
        final LocalDate predictionDate = LocalDate.of(2023, 11, 6);
        final CurrencyType currencyType = CurrencyType.EUR;
        final List<RatePrediction> mockedData = List.of(
                new RatePrediction(predictionDate.minusDays(1), BigDecimal.valueOf(2)),
                new RatePrediction(predictionDate.minusDays(2), BigDecimal.valueOf(4))
        );

        final RatePredictionDao ratePredictionDao = Mockito.mock(RatePredictionDao.class);
        final LinearRegression<LocalDate, BigDecimal> linearRegression = Mockito.mock(LinearRegression.class);
        final RatePredictor ratePredictor = new LinearRegressionRatePredictor(ratePredictionDao, linearRegression, DEFAULT_DEPTH);
        Mockito.when(ratePredictionDao.getFirstOrderByDateDesc(currencyType, DEFAULT_DEPTH)).thenReturn(mockedData);
        Mockito.when(linearRegression.predict(predictionDate)).thenReturn(BigDecimal.ONE);

        //when + then
        assertThatThrownBy(() -> ratePredictor.predict(currencyType, predictionDate, null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}