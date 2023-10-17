package ru.liga.rateprediction.core;

import lombok.Builder;
import lombok.Value;

/**
 * Params for {@link MeanRatePredictor} algorithm
 */
@Value
@Builder
public class MeanRatePredictorParams {
    @Builder.Default
    int depth = 7;
}
