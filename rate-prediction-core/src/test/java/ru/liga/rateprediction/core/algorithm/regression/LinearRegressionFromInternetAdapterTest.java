package ru.liga.rateprediction.core.algorithm.regression;

import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

class LinearRegressionFromInternetAdapterTest {
    public static Stream<Arguments> linearRegressionVariants() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(
                        new LocalDate[]{
                                LocalDate.of(1970, 1, 2), //1
                                LocalDate.of(1970, 1, 3), //2
                                LocalDate.of(1970, 1, 4)  //3
                        },
                        new BigDecimal[]{
                                BigDecimal.valueOf(2),
                                BigDecimal.valueOf(4),
                                BigDecimal.valueOf(6)
                        },
                        LocalDate.of(1970, 1, 5), BigDecimal.valueOf(8)
                ))
//                .add(Arguments.of(
//                        new LocalDate[]{
//                                LocalDate.of(1970, 1, 2), //1
//                                LocalDate.of(1970, 1, 3), //2
//                                LocalDate.of(1970, 1, 4)  //3
//                        },
//                        new BigDecimal[]{
//                                BigDecimal.valueOf(1),
//                                BigDecimal.valueOf(0.5),
//                                BigDecimal.valueOf(0.33)
//                        },
//                        LocalDate.of(1970, 1, 5), BigDecimal.valueOf(0.25)
//                ))
                .build();
    }

    @Test
    void predict_whenIsNotInit_thenThrowIAE() {
        //given
        final LinearRegression<LocalDate, BigDecimal> linearRegression = new LinearRegressionFromInternetAdapter<>();

        //when + then
        assertThatThrownBy(() -> linearRegression.predict(LocalDate.now())).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @MethodSource("linearRegressionVariants")
    void predict_whenIsInit_thenDoLinearRegression(LocalDate[] dates,
                                                   BigDecimal[] rates,
                                                   LocalDate localDate,
                                                   BigDecimal expected) {
        //given
        final LinearRegression<LocalDate, BigDecimal> linearRegression = new LinearRegressionFromInternetAdapter<>();

        //when
        linearRegression.init(dates, rates);
        final BigDecimal actual = linearRegression.predict(localDate);

        //then
        assertThat(actual).isCloseTo(expected, Offset.offset(BigDecimal.valueOf(0.01)));
    }
}