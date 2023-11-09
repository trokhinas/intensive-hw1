package ru.liga.rateprediction.core.dao;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.liga.rateprediction.core.CurrencyType;
import ru.liga.rateprediction.core.RatePrediction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class CsvCBRFRatePredictionDaoTest {
    private final Map<CurrencyType, String> filePaths = Map.of(CurrencyType.EUR, "/dao/CsvCBRFRatePredictionDao/csv-cbrf-rate-prediction-dao.csv");
    private final CsvParserParams parserParams = CsvParserParams.builder().separator(';').quoteCharacter('\"').build();
    private final CurrencyType currencyType = CurrencyType.EUR;

    public static Stream<Arguments> dateVariants() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(
                        LocalDate.of(2023, 10, 28),
                        new RatePrediction(LocalDate.of(2023, 10, 28), BigDecimal.valueOf(00.2316d))
                ))
                .add(Arguments.of(
                        LocalDate.of(2023, 10, 30),
                        null
                ))
                .build();
    }

    public static Stream<Arguments> countVariants() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(
                        0, List.of()
                ))
                .add(Arguments.of(
                        1,
                        List.of(
                                new RatePrediction(LocalDate.of(2023, 10, 28), BigDecimal.valueOf(00.2316d))
                        )
                ))
                .add(Arguments.of(
                        2,
                        List.of(
                                new RatePrediction(LocalDate.of(2023, 10, 28), BigDecimal.valueOf(00.2316d)),
                                new RatePrediction(LocalDate.of(2023, 10, 27), BigDecimal.valueOf(00.2325d))
                        )
                ))
                .add(Arguments.of(
                        3,
                        List.of(
                                new RatePrediction(LocalDate.of(2023, 10, 28), BigDecimal.valueOf(00.2316d)),
                                new RatePrediction(LocalDate.of(2023, 10, 27), BigDecimal.valueOf(00.2325d)),
                                new RatePrediction(LocalDate.of(2023, 10, 26), BigDecimal.valueOf(00.2315d))
                        )
                ))
                .add(Arguments.of(
                        4,
                        List.of(
                                new RatePrediction(LocalDate.of(2023, 10, 28), BigDecimal.valueOf(00.2316d)),
                                new RatePrediction(LocalDate.of(2023, 10, 27), BigDecimal.valueOf(00.2325d)),
                                new RatePrediction(LocalDate.of(2023, 10, 26), BigDecimal.valueOf(00.2315d))
                        )
                ))
                .build();
    }

    @Test
    void findAllByCurrencyType_whenCall_thenReturnAllLines() {
        //given
        final RatePredictionDao ratePredictionDao = new CsvCBRFRatePredictionDao(filePaths, parserParams);
        final List<RatePrediction> expected = List.of(
                new RatePrediction(LocalDate.of(2023, 10, 28), BigDecimal.valueOf(00.2316d)),
                new RatePrediction(LocalDate.of(2023, 10, 27), BigDecimal.valueOf(00.2325d)),
                new RatePrediction(LocalDate.of(2023, 10, 26), BigDecimal.valueOf(00.2315d))
        );

        //when
        final List<RatePrediction> actual = ratePredictionDao.findAllByCurrencyType(currencyType);

        //then
        assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
    }

    @ParameterizedTest
    @MethodSource("dateVariants")
    void findByCurrencyTypeAndDate_whenPresent_thenReturn(LocalDate localDate, RatePrediction expected) {
        //given
        final RatePredictionDao ratePredictionDao = new CsvCBRFRatePredictionDao(filePaths, parserParams);

        //when
        final Optional<RatePrediction> actual = ratePredictionDao.findByCurrencyTypeAndDate(currencyType, localDate);

        //then
        assertThat(actual).isEqualTo(Optional.ofNullable(expected));
    }

    @ParameterizedTest
    @MethodSource("countVariants")
    void getFirstOrderByDateDesc_whenPresent_thenReturnSorted(int count, List<RatePrediction> expected) {
        //given
        final RatePredictionDao ratePredictionDao = new CsvCBRFRatePredictionDao(filePaths, parserParams);

        //when
        final List<RatePrediction> actual = ratePredictionDao.getFirstOrderByDateDesc(currencyType, count);

        //then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void findAllByCurrencyType_whenPathIsNotFound_thenThrowIAE() {
        //given
        final RatePredictionDao ratePredictionDao = new CsvCBRFRatePredictionDao(
                Map.of(),
                parserParams
        );

        //when + then
        assertThatThrownBy(() -> ratePredictionDao.findAllByCurrencyType(currencyType))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void findAllByCurrencyType_whenFileIsNotFound_thenThrowIAE() {
        //given
        final RatePredictionDao ratePredictionDao = new CsvCBRFRatePredictionDao(
                Map.of(currencyType, "not-existed-file.csv"),
                parserParams
        );

        //when + then
        assertThatThrownBy(() -> ratePredictionDao.findAllByCurrencyType(currencyType))
                .isInstanceOf(IllegalArgumentException.class);
    }
}