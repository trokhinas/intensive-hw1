package ru.liga.rateprediction.core.algorithm;

import org.apache.commons.lang3.NotImplementedException;

import java.util.Map;

public class RatePredictorFactory {
    private final Map<RatePredictionAlgorithm, RatePredictor> algorithmRatePredictorMap;

    public RatePredictorFactory(Map<RatePredictionAlgorithm, RatePredictor> algorithmRatePredictorMap) {
        this.algorithmRatePredictorMap = algorithmRatePredictorMap;
    }

    public RatePredictorFactory() {
        this.algorithmRatePredictorMap = Map.of(
                RatePredictionAlgorithm.MEAN, new MeanRatePredictor()
        );
    }

    public RatePredictor create(RatePredictionAlgorithm algorithm) {
        final RatePredictor ratePredictor = algorithmRatePredictorMap.get(algorithm);
        if (ratePredictor == null) {
            throw new NotImplementedException(String.format(
                    "Algorithm = %s is not implemented yet!", algorithm
            ));
        }

        return ratePredictor;
    }
}
