package ru.liga.rateprediction.core.algorithm;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;

import java.util.Map;

@Slf4j
public class RatePredictorFactory {
    private final Map<RatePredictionAlgorithm, RatePredictor> algorithmRatePredictorMap;

    public RatePredictorFactory(Map<RatePredictionAlgorithm, RatePredictor> algorithmRatePredictorMap) {
        this.algorithmRatePredictorMap = algorithmRatePredictorMap;
        log.info("Initialised rate predictor factory for algorithms = {}", algorithmRatePredictorMap);
    }

    public RatePredictorFactory() {
        this(Map.of(RatePredictionAlgorithm.MEAN, new MeanRatePredictor()));
    }

    public RatePredictor create(RatePredictionAlgorithm algorithm) {
        log.info("Create rate predictor for algorithm = {}", algorithm);
        final RatePredictor ratePredictor = algorithmRatePredictorMap.get(algorithm);
        if (ratePredictor == null) {
            throw new NotImplementedException(String.format(
                    "Algorithm = %s is not implemented yet!", algorithm
            ));
        }

        return ratePredictor;
    }
}
