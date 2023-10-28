package ru.liga.rateprediction.telegram.controller.commands.adapt;

import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public interface TelegramMessageAdapter {
    void execute(AbsSender absSender) throws TelegramApiException;
}
