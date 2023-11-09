package ru.liga.rateprediction.telegram;

import lombok.extern.slf4j.Slf4j;
import ru.liga.rateprediction.telegram.controller.RatePredictionTelegram;

@Slf4j
public class Main {
    public static void main(String[] args) {
        RatePredictionTelegram.start();
        log.info("Telegram bot started");
    }
}
