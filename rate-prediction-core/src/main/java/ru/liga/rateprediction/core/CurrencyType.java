package ru.liga.rateprediction.core;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

/**
 * That enum represents supported currency types
 */
@Getter
@RequiredArgsConstructor
public enum CurrencyType {
    EUR("EUR"),
    USD("USD"),
    TRY("TRY");

    /**
     * Abbreviation code that mapped to enum object
     */
    private final String code;

    /**
     * Method tries to determine {@link CurrencyType} enum object by provided {@link String} code.
     *
     * @param code provided String code, nullable
     * @return {@link Optional} that contains {@link CurrencyType} object that mapped to provided currency abbreviation
     * or {@link Optional#empty()} if there is no currency mapped to provided currency abbreviation code
     */
    public static Optional<CurrencyType> byCode(String code) {
        return Arrays.stream(values())
                .filter(x -> x.code.equalsIgnoreCase(code))
                .findFirst();
    }
}
