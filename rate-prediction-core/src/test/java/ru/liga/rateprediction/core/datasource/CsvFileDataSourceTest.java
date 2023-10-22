package ru.liga.rateprediction.core.datasource;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;
import com.opencsv.exceptions.CsvException;
import lombok.Data;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;
import ru.liga.rateprediction.core.RatePrediction;
import ru.liga.rateprediction.core.datasource.files.csv.CsvParserParams;
import ru.liga.rateprediction.core.datasource.files.csv.CsvToBeanReader;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class CsvFileDataSourceTest {

    @Test
    void getData_whenNoFile_thenThrowIAE() {
        //given
        final String notExistedPath = "qwe.csv";
        final CsvFileDataSource<MyCsvBean> csvFileDataSource = new CsvFileDataSource<>(
                CsvToBeanReader.openCSV(),
                CsvParserParams.builder().separator(',').build(),
                MyCsvBean.class,
                myCsvBean -> new RatePrediction(myCsvBean.getDate(), myCsvBean.getValue()),
                notExistedPath
        );

        //when + then
        assertThatThrownBy(() -> csvFileDataSource.getData(1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasNoCause();
    }

    @Test
    void getData_whenFileIsPresentButWrongBean_thenThrow() {
        //given
        final String wrongFormatPath = "/datasource/wrong-format.csv";
        final CsvFileDataSource<MyCsvBean> csvFileDataSource = new CsvFileDataSource<>(
                CsvToBeanReader.openCSV(),
                CsvParserParams.builder().separator(',').build(),
                MyCsvBean.class,
                myCsvBean -> new RatePrediction(myCsvBean.getDate(), myCsvBean.getValue()),
                wrongFormatPath
        );

        //when + then
        assertThatThrownBy(() -> csvFileDataSource.getData(1))
                .isInstanceOf(RuntimeException.class)
                .hasCauseInstanceOf(CsvException.class);
    }

    @Test
    void getData_whenFileIsOk_thenReturnRows() {
        //given
        final String filePath = "/datasource/beans.csv";
        final CsvFileDataSource<MyCsvBean> csvFileDataSource = new CsvFileDataSource<>(
                CsvToBeanReader.openCSV(),
                CsvParserParams.builder().separator(',').build(),
                MyCsvBean.class,
                myCsvBean -> new RatePrediction(myCsvBean.getDate(), myCsvBean.getValue()),
                filePath
        );
        final List<RatePrediction> expected = List.of(
                new RatePrediction(LocalDate.of(2023, 10, 22), BigDecimal.valueOf(123.456)),
                new RatePrediction(LocalDate.of(2023, 10, 23), BigDecimal.valueOf(234.567)),
                new RatePrediction(LocalDate.of(2023, 10, 24), BigDecimal.valueOf(345.678))
        );

        //when
        final List<RatePrediction> actual = csvFileDataSource.getData(5);

        //then
        assertThat(actual)
                .asInstanceOf(InstanceOfAssertFactories.list(RatePrediction.class))
                .containsExactlyInAnyOrderElementsOf(expected);
    }

    @Data
    public static class MyCsvBean {
        @CsvDate("dd/MM/yyyy")
        @CsvBindByName(column = "date", required = true)
        private LocalDate date;

        @CsvBindByName(column = "value", required = true)
        private BigDecimal value;
    }
}