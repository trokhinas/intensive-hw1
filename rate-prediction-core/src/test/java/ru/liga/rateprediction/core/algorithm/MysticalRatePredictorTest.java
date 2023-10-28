package ru.liga.rateprediction.core.algorithm;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import ru.liga.rateprediction.core.CurrencyType;
import ru.liga.rateprediction.core.RatePrediction;
import ru.liga.rateprediction.core.dao.RatePredictionDao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

class MysticalRatePredictorTest {

    @Test
    void predictSingle_whenDataIsProvided_thenReturnRandomYear() {
        //given
        final LocalDate predictionDate = LocalDate.of(2023, 11, 2);
        final CurrencyType currencyType = CurrencyType.EUR;
        final List<RatePrediction> mockedData = List.of(
                new RatePrediction(predictionDate.minusYears(1), BigDecimal.ONE), //2022
                new RatePrediction(predictionDate.minusYears(2), BigDecimal.valueOf(2)), //2021
                new RatePrediction(predictionDate.minusYears(3), BigDecimal.valueOf(3)), //2020
                new RatePrediction(predictionDate.minusYears(4), BigDecimal.valueOf(4)), //2019
                new RatePrediction(predictionDate.minusYears(5), BigDecimal.valueOf(5)) //2018
        );
        final RatePrediction expected = new RatePrediction(predictionDate, BigDecimal.valueOf(4));

        final RatePredictionDao ratePredictionDao = Mockito.mock(RatePredictionDao.class);
        final Random random = Mockito.mock(Random.class);
        final RatePredictor ratePredictor = new MysticalRatePredictor(ratePredictionDao, random);
        Mockito.when(ratePredictionDao.findAllByCurrencyType(currencyType)).thenReturn(mockedData);
        Mockito.when(random.nextInt(2018, 2023))
                .thenReturn(2018)
                .thenReturn(2019)
                .thenReturn(2020)
                .thenReturn(2021)
                .thenReturn(2022);
        Mockito.when(ratePredictionDao.findByCurrencyTypeAndDate(eq(currencyType), any()))
                .thenReturn(Optional.empty());
        Mockito.when(ratePredictionDao.findByCurrencyTypeAndDate(eq(currencyType), eq(predictionDate.withYear(2022))))
                .thenReturn(Optional.ofNullable(mockedData.get(3)));

        //when
        final List<RatePrediction> actual = ratePredictor.predict(currencyType, predictionDate, null);

        //then
        assertThat(actual).containsExactly(expected);
        Mockito.verify(ratePredictionDao).findAllByCurrencyType(currencyType);
        Mockito.verify(ratePredictionDao).findByCurrencyTypeAndDate(currencyType, predictionDate.withYear(2018));
        Mockito.verify(ratePredictionDao).findByCurrencyTypeAndDate(currencyType, predictionDate.withYear(2019));
        Mockito.verify(ratePredictionDao).findByCurrencyTypeAndDate(currencyType, predictionDate.withYear(2020));
        Mockito.verify(ratePredictionDao).findByCurrencyTypeAndDate(currencyType, predictionDate.withYear(2021));
        Mockito.verify(ratePredictionDao).findByCurrencyTypeAndDate(currencyType, predictionDate.withYear(2022));
        Mockito.verifyNoMoreInteractions(ratePredictionDao);
    }

    @Test
    void predictRange_whenDataIsProvided_thenReturnPredictions() {
        //given
        final LocalDate predictionDateStart = LocalDate.of(2023, 11, 2);
        final LocalDate predictionDateEnd = LocalDate.of(2023, 11, 4);
        final CurrencyType currencyType = CurrencyType.EUR;
        final List<RatePrediction> mockedData = List.of(
                new RatePrediction(predictionDateStart.minusYears(1), BigDecimal.ONE), //2022
                new RatePrediction(predictionDateStart.minusYears(2), BigDecimal.valueOf(2)), //2021
                new RatePrediction(predictionDateStart.minusYears(3), BigDecimal.valueOf(3)), //2020
                new RatePrediction(predictionDateStart.minusYears(4), BigDecimal.valueOf(4)), //2019
                new RatePrediction(predictionDateStart.minusYears(5), BigDecimal.valueOf(5)) //2018
        );
        final List<RatePrediction> expected = List.of(
                new RatePrediction(predictionDateStart, BigDecimal.valueOf(4)),
                new RatePrediction(predictionDateStart.plusDays(1), BigDecimal.valueOf(2)),
                new RatePrediction(predictionDateEnd, BigDecimal.valueOf(2))
        );

        final RatePredictionDao ratePredictionDao = Mockito.mock(RatePredictionDao.class);
        final Random random = Mockito.mock(Random.class);
        final RatePredictor ratePredictor = new MysticalRatePredictor(ratePredictionDao, random);
        Mockito.when(ratePredictionDao.findAllByCurrencyType(currencyType)).thenReturn(mockedData);
        Mockito.when(random.nextInt(2018, 2023))
                .thenReturn(2019) // first attempt for first date
                .thenReturn(2018)
                .thenReturn(2019)
                .thenReturn(2021) // third attempt for second date
                .thenReturn(2018)
                .thenReturn(2019)
                .thenReturn(2020)
                .thenReturn(2022)
                .thenReturn(2021); // last attempt for third date
        Mockito.when(ratePredictionDao.findByCurrencyTypeAndDate(eq(currencyType), any()))
                .thenReturn(Optional.empty());
        Mockito.when(ratePredictionDao.findByCurrencyTypeAndDate(currencyType, predictionDateStart.withYear(2019)))
                .thenReturn(Optional.of(mockedData.get(3)));
        Mockito.when(ratePredictionDao.findByCurrencyTypeAndDate(currencyType, predictionDateStart.plusDays(1).withYear(2021)))
                .thenReturn(Optional.of(mockedData.get(1)));
        Mockito.when(ratePredictionDao.findByCurrencyTypeAndDate(currencyType, predictionDateEnd.withYear(2021)))
                .thenReturn(Optional.of(mockedData.get(1)));

        //when
        final List<RatePrediction> actual = ratePredictor.predict(currencyType, predictionDateStart, predictionDateEnd);

        //then
        final LocalDate secondDate = predictionDateStart.plusDays(1);
        assertThat(actual).containsExactlyElementsOf(expected);
        Mockito.verify(ratePredictionDao).findAllByCurrencyType(currencyType);

        Mockito.verify(ratePredictionDao).findByCurrencyTypeAndDate(currencyType, predictionDateStart.withYear(2019));

        Mockito.verify(ratePredictionDao).findByCurrencyTypeAndDate(currencyType, secondDate.withYear(2018));
        Mockito.verify(ratePredictionDao).findByCurrencyTypeAndDate(currencyType, secondDate.withYear(2019));
        Mockito.verify(ratePredictionDao).findByCurrencyTypeAndDate(currencyType, secondDate.withYear(2021));

        Mockito.verify(ratePredictionDao).findByCurrencyTypeAndDate(currencyType, predictionDateEnd.withYear(2018));
        Mockito.verify(ratePredictionDao).findByCurrencyTypeAndDate(currencyType, predictionDateEnd.withYear(2019));
        Mockito.verify(ratePredictionDao).findByCurrencyTypeAndDate(currencyType, predictionDateEnd.withYear(2020));
        Mockito.verify(ratePredictionDao).findByCurrencyTypeAndDate(currencyType, predictionDateEnd.withYear(2022));
        Mockito.verify(ratePredictionDao).findByCurrencyTypeAndDate(currencyType, predictionDateEnd.withYear(2021));
        Mockito.verifyNoMoreInteractions(ratePredictionDao);
    }

    @Test
    void predictSingle_whenNoDataIsProvided_thenThrowIAE() {
        //given
        final LocalDate predictionDate = LocalDate.now();
        final CurrencyType currencyType = CurrencyType.EUR;

        final RatePredictionDao ratePredictionDao = Mockito.mock(RatePredictionDao.class);
        final Random random = Mockito.mock(Random.class);
        final RatePredictor ratePredictor = new MysticalRatePredictor(ratePredictionDao, random);
        Mockito.when(ratePredictionDao.findAllByCurrencyType(currencyType)).thenReturn(List.of());

        //when + then
        assertThatThrownBy(() -> ratePredictor.predict(currencyType, predictionDate, null))
                .isInstanceOf(IllegalArgumentException.class);
        Mockito.verify(ratePredictionDao).findAllByCurrencyType(currencyType);
        Mockito.verifyNoMoreInteractions(ratePredictionDao);
    }

    // cause of random we can potentially skip NPE test case add repeat 50 times
    @RepeatedTest(50)
    void predictSingle_whenNotFoundDateInNAttempts_thenThrowIAE() {
        //given
        final LocalDate predictionDate = LocalDate.of(2023, 11, 2);
        final CurrencyType currencyType = CurrencyType.EUR;
        final List<RatePrediction> mockedData = List.of(
                new RatePrediction(predictionDate.minusYears(1), BigDecimal.ONE), //2022
                new RatePrediction(predictionDate.minusYears(2), BigDecimal.valueOf(2)), //2021
                new RatePrediction(predictionDate.minusYears(3), BigDecimal.valueOf(3)), //2020
                new RatePrediction(predictionDate.minusYears(4), BigDecimal.valueOf(4)), //2019
                new RatePrediction(predictionDate.minusYears(5), BigDecimal.valueOf(5)) //2018
        );

        final RatePredictionDao ratePredictionDao = Mockito.mock(RatePredictionDao.class);
        final Random random = new Random(System.currentTimeMillis());
        final RatePredictor ratePredictor = new MysticalRatePredictor(ratePredictionDao, random);
        Mockito.when(ratePredictionDao.findAllByCurrencyType(currencyType)).thenReturn(mockedData);
        Mockito.when(ratePredictionDao.findByCurrencyTypeAndDate(eq(currencyType), ArgumentMatchers.argThat(localDateInYears(2018, 2022))))
                .thenReturn(Optional.empty());

        //when + then
        assertThatThrownBy(() -> ratePredictor.predict(currencyType, predictionDate, null))
                .isInstanceOf(IllegalArgumentException.class);
        Mockito.verify(ratePredictionDao).findAllByCurrencyType(currencyType);
        Mockito.verify(ratePredictionDao, Mockito.times(5)).findByCurrencyTypeAndDate(any(), any());
        Mockito.verifyNoMoreInteractions(ratePredictionDao);
    }

    private static ArgumentMatcher<LocalDate> localDateInYears(int yearFromInclusive, int yearToInclusive) {
        return localDate -> localDate != null
                && (localDate.getYear() >= yearFromInclusive || localDate.getYear() <= yearToInclusive);
    }
}