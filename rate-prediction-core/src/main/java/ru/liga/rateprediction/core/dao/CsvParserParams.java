package ru.liga.rateprediction.core.dao;

import lombok.Builder;
import lombok.Value;

/**
 * Class that provides params for CSV parsing
 */
@Value
@Builder
public class CsvParserParams {
    @Builder.Default
    char separator = ';';

    @Builder.Default
    char escapeCharacter = '\\';

    @Builder.Default
    char quoteCharacter = '\"';
}
