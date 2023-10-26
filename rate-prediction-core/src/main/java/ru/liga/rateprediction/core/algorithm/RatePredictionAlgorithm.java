package ru.liga.rateprediction.core.algorithm;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RatePredictionAlgorithm {
    MEAN("mean");

    private final String code;
}
