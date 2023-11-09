package ru.liga.rateprediction.telegram.controller.commands.adapt;

import lombok.Builder;
import lombok.Value;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Value
@Builder
public class TextTelegramMessageAdapter implements TelegramMessageAdapter {
    String text;
    Long chatId;
    @Override
    public void execute(AbsSender absSender) throws TelegramApiException {
        final SendMessage sendMessage = new SendMessage();
        sendMessage.setText(text);
        sendMessage.setChatId(chatId);
        absSender.execute(sendMessage);
    }
}
