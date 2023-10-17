package ru.liga.rateprediction.core;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

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
     * @return {@link CurrencyType} object that mapped to provided currency abbreviation
     * @throws EnumConstantNotPresentException if there is no currency mapped to provided currency abbreviation code
     */
    public static CurrencyType byCode(String code) {
        return Arrays.stream(values())
                .filter(x -> x.code.equalsIgnoreCase(code))
                .findFirst()
                .orElseThrow(() -> new EnumConstantNotPresentException(CurrencyType.class, code));
    }
}
