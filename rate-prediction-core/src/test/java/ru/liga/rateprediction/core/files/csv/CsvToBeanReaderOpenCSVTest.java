package ru.liga.rateprediction.core.files.csv;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
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
    void testReading(String description,
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
    void whenCountIsNotPositive_thenThrows() {
        //given
        //no setup

        //when + then
        assertThatThrownBy(() -> csvToBeanReader.readLines(
                new ByteArrayInputStream("mock".getBytes()),
                CsvParserParams.builder().build(),
                MyOpenCSVBean.class,
                0
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void whenCountIsGreaterThanCountOfLines_thenReturnLines() throws IOException {
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