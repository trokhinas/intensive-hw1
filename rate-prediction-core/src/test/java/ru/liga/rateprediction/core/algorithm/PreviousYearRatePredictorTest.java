package ru.liga.rateprediction.core.algorithm;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import ru.liga.rateprediction.core.CurrencyType;
import ru.liga.rateprediction.core.RatePrediction;
import ru.liga.rateprediction.core.dao.RatePredictionDao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class PreviousYearRatePredictorTest {

    @Test
    void predictSingle_whenFoundPreviousYearValue_thenReturn() {
        //given
        final LocalDate predictionDate = LocalDate.now();
        final CurrencyType currencyType = CurrencyType.EUR;

        final LocalDate prevYearDate = predictionDate.minusYears(1);
        final BigDecimal prevYearRate = BigDecimal.ONE;
        final RatePrediction expected = new RatePrediction(predictionDate, prevYearRate);

        final RatePredictionDao ratePredictionDao = Mockito.mock(RatePredictionDao.class);
        final RatePredictor ratePredictor = new PreviousYearRatePredictor(ratePredictionDao);
        Mockito.when(ratePredictionDao.findByCurrencyTypeAndDate(currencyType, prevYearDate))
                .thenAnswer(answerWithRate(prevYearRate));

        //when
        final List<RatePrediction> actual = ratePredictor.predict(currencyType, predictionDate, null);

        //then
        assertThat(actual).containsExactly(expected);
    }

    @Test
    void predictRange_whenFoundPreviousYearValue_thenReturn() {
        //given
        final LocalDate predictionDateStart = LocalDate.now();
        final LocalDate predictionDateEnd = predictionDateStart.plusDays(1);
        final CurrencyType currencyType = CurrencyType.EUR;

        //first prediction
        final LocalDate firstPrevYearDate = predictionDateStart.minusYears(1);
        final LocalDate firstPrevYearPrevDayDate = firstPrevYearDate.minusDays(1);
        final BigDecimal firstPrevYearPrevDayRate = BigDecimal.ONE;

        final LocalDate secondPrevYearDate = predictionDateEnd.minusYears(1);
        final BigDecimal secondPrevYearRate = BigDecimal.TEN;

        final List<RatePrediction> expected = List.of(
                new RatePrediction(predictionDateStart, firstPrevYearPrevDayRate),
                new RatePrediction(predictionDateEnd, secondPrevYearRate)
        );

        final RatePredictionDao ratePredictionDao = Mockito.mock(RatePredictionDao.class);
        final RatePredictor ratePredictor = new PreviousYearRatePredictor(ratePredictionDao);
        //start -> return previous year previous date
        Mockito.when(ratePredictionDao.findByCurrencyTypeAndDate(currencyType, firstPrevYearDate))
                .thenReturn(Optional.empty());
        Mockito.when(ratePredictionDao.findByCurrencyTypeAndDate(currencyType, firstPrevYearPrevDayDate))
                .thenAnswer(answerWithRate(firstPrevYearPrevDayRate));
        //end -> return previous year
        Mockito.when(ratePredictionDao.findByCurrencyTypeAndDate(currencyType, secondPrevYearDate))
                .thenAnswer(answerWithRate(secondPrevYearRate));

        //when
        final List<RatePrediction> actual = ratePredictor.predict(currencyType, predictionDateStart, predictionDateEnd);

        //then
        assertThat(actual).containsExactlyElementsOf(expected);
    }

    @Test
    void predictSingle_whenNotFoundPreviousYear_thenTryToFoundPreviousDay() {
        //given
        final LocalDate predictionDate = LocalDate.now();
        final CurrencyType currencyType = CurrencyType.EUR;

        final LocalDate prevYearDate = predictionDate.minusYears(1);
        final LocalDate prevYearPrevDayDate = prevYearDate.minusDays(1);
        final BigDecimal prevYearPrevDayRate = BigDecimal.ONE;
        final RatePrediction expected = new RatePrediction(predictionDate, prevYearPrevDayRate);

        final RatePredictionDao ratePredictionDao = Mockito.mock(RatePredictionDao.class);
        final RatePredictor ratePredictor = new PreviousYearRatePredictor(ratePredictionDao);
        Mockito.when(ratePredictionDao.findByCurrencyTypeAndDate(currencyType, prevYearDate))
                .thenReturn(Optional.empty());
        Mockito.when(ratePredictionDao.findByCurrencyTypeAndDate(currencyType, prevYearPrevDayDate))
                .thenAnswer(answerWithRate(prevYearPrevDayRate));

        //when
        final List<RatePrediction> actual = ratePredictor.predict(currencyType, predictionDate, null);

        //then
        assertThat(actual).containsExactly(expected);
    }

    @Test
    void predictSingle_whenNotFoundPreviousYearAndPreviousDay_thenThrowIAE() {
        //given
        final LocalDate predictionDate = LocalDate.now();
        final CurrencyType currencyType = CurrencyType.EUR;

        final LocalDate prevYearDate = predictionDate.minusYears(1);
        final LocalDate prevYearPrevDayDate = prevYearDate.minusDays(1);

        final RatePredictionDao ratePredictionDao = Mockito.mock(RatePredictionDao.class);
        final RatePredictor ratePredictor = new PreviousYearRatePredictor(ratePredictionDao);
        Mockito.when(ratePredictionDao.findByCurrencyTypeAndDate(currencyType, prevYearDate))
                .thenReturn(Optional.empty());
        Mockito.when(ratePredictionDao.findByCurrencyTypeAndDate(currencyType, prevYearPrevDayDate))
                .thenReturn(Optional.empty());

        //when + then
        assertThatThrownBy(() -> ratePredictor.predict(currencyType, predictionDate, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private Answer<Optional<RatePrediction>> answerWithRate(BigDecimal rate) {
        return invocationOnMock -> {
            final LocalDate localDate = invocationOnMock.getArgument(1);
            return Optional.of(new RatePrediction(localDate, rate));
        };
    }
}