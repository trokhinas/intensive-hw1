package ru.liga.rateprediction.core.files.csv;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.liga.rateprediction.core.datasource.files.csv.CsvParserParams;
import ru.liga.rateprediction.core.datasource.files.csv.CsvToBeanReader;
import ru.liga.rateprediction.core.datasource.files.csv.RateCBRFCsvRow;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class CsvToBeanReaderOpenCSVTest {
    private final CsvToBeanReader csvToBeanReader = CsvToBeanReader.openCSV();

    public static Stream<Arguments> readingVariants() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(
                        "Semicolon separated file with quotes",
                        "column1;column2;column3\nsimple string;1;10/16/2023\nstring with \"\"quotes\"\";2;10/17/2023",
                        CsvParserParams.builder().separator(';').quoteCharacter('"').build(),
                        2,
                        List.of(
                                new MyOpenCSVBean("simple string", 1, LocalDate.of(2023, 10, 16)),
                                new MyOpenCSVBean("string with \"quotes\"", 2, LocalDate.of(2023, 10, 17))
                        )
                ))
                .add(Arguments.of(
                        "Comma separated file with quotes",
                        "column1,column2,column3\nsimple string,1,10/16/2023\nstring with \"\"quotes\"\",2,10/17/2023",
                        CsvParserParams.builder().separator(',').quoteCharacter('"').build(),
                        2,
                        List.of(
                                new MyOpenCSVBean("simple string", 1, LocalDate.of(2023, 10, 16)),
                                new MyOpenCSVBean("string with \"quotes\"", 2, LocalDate.of(2023, 10, 17))
                        )
                ))
                .build();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("readingVariants")
    void readLines_whenDataIsOk_thenReturnBeans(String description,
                                                String csvText,
                                                CsvParserParams params,
                                                int count,
                                                List<MyOpenCSVBean> expected) throws IOException {
        //given
        //no setup

        //when
        final InputStream inputStream = new ByteArrayInputStream(csvText.getBytes(StandardCharsets.UTF_8));
        final List<MyOpenCSVBean> actual = csvToBeanReader.readLines(
                inputStream,
                params,
                MyOpenCSVBean.class,
                count
        );

        //then
        assertThat(actual).containsExactlyElementsOf(expected);
    }

    @Test
    void readLines_whenCountIsNotPositive_thenThrowsIAE() {
        //given
        //no setup

        //when + then
        assertThatThrownBy(() -> csvToBeanReader.readLines(
                new ByteArrayInputStream("mock".getBytes()),
                CsvParserParams.builder().build(),
                MyOpenCSVBean.class,
                0
        )).isInstanceOf(IllegalArgumentException.class).hasNoCause();
    }

    @Test
    void readLines_whenCountIsGreaterThanCountOfLines_thenReturnFewerLines() throws IOException {
        //given
        final String csvText = "column1;column2;column3\nstring1;1;10/16/2023";
        final InputStream inputStream = new ByteArrayInputStream(csvText.getBytes(StandardCharsets.UTF_8));

        //when
        final List<MyOpenCSVBean> actual = csvToBeanReader.readLines(
                inputStream,
                CsvParserParams.builder().separator(';').build(),
                MyOpenCSVBean.class,
                100_000
        );

        //then
        assertThat(actual).isNotNull()
                .containsExactly(new MyOpenCSVBean(
                        "string1",
                        1,
                        LocalDate.of(2023, 10, 16)
                ));
    }

    @Test
    void readLines_whenReadRateCBRFCsvRow_thenReturnRows() throws IOException {
        //given
        final String csvText = """
                "nominal";"data";"curs";"cdx"
                1;10/13/2023;103.0350;"Евро"
                1;10/12/2023;105.9544;"Евро"
                1;10/11/2023;105.6864;"Евро"
                1;10/10/2023;107.0322;"Евро"
                """;
        final InputStream inputStream = new ByteArrayInputStream(csvText.getBytes(StandardCharsets.UTF_8));

        //when
        final List<RateCBRFCsvRow> actual = csvToBeanReader.readLines(
                inputStream,
                CsvParserParams.builder().separator(';').build(),
                RateCBRFCsvRow.class,
                7
        );

        //then
        final RecursiveComparisonConfiguration configuration = new RecursiveComparisonConfiguration();
        configuration.registerComparatorForType(BigDecimal::compareTo, BigDecimal.class);
        assertThat(actual)
                .usingRecursiveFieldByFieldElementComparator(configuration)
                .containsExactly(
                        new RateCBRFCsvRow(
                                1, LocalDate.of(2023, 10, 13),
                                BigDecimal.valueOf(103.0350), "Евро"
                        ),
                        new RateCBRFCsvRow(
                                1, LocalDate.of(2023, 10, 12),
                                BigDecimal.valueOf(105.9544), "Евро"
                        ),
                        new RateCBRFCsvRow(
                                1, LocalDate.of(2023, 10, 11),
                                BigDecimal.valueOf(105.6864), "Евро"
                        ),
                        new RateCBRFCsvRow(
                                1, LocalDate.of(2023, 10, 10),
                                BigDecimal.valueOf(107.0322), "Евро"
                        )
                );
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MyOpenCSVBean {
        @CsvBindByName(column = "column1")
        private String stringColumn;

        @CsvBindByName(column = "column2")
        private Integer intColumn;

        @CsvDate("MM/dd/yyyy")
        @CsvBindByName(column = "column3")
        private LocalDate localDateColumn;
    }
}