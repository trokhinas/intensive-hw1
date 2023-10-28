package ru.liga.rateprediction.core.algorithm;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.liga.rateprediction.core.CurrencyType;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum RatePredictionAlgorithm {
    MEAN("mean"),
    MYSTICAL("mist"),
    PREVIOUS("prev"),
    LINEAR_REGRESSION("lin_reg");

    private final String code;

    /**
     * Method tries to determine {@link RatePredictionAlgorithm} enum object by provided {@link String} code.
     *
     * @param code short code of algorithm
     * @return {@link RatePredictionAlgorithm} object that mapped to provided short code
     * @throws EnumConstantNotPresentException if there is no algorithm mapped to provided short code
     */
    public static RatePredictionAlgorithm byCode(String code) {
        return Arrays.stream(values())
                .filter(x -> x.code.equalsIgnoreCase(code))
                .findFirst()
                .orElseThrow(() -> new EnumConstantNotPresentException(RatePredictionAlgorithm.class, code));
    }
}
