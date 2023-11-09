package ru.liga.rateprediction.telegram.controller.commands;

import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.liga.rateprediction.telegram.controller.commands.adapt.TelegramMessageAdapter;
import ru.liga.rateprediction.telegram.controller.commands.adapt.TextTelegramMessageAdapter;

public class StartCommand extends AbstractBotCommand {
    public StartCommand() {
        super("start", "Старт");
    }

    @Override
    protected TelegramMessageAdapter prepareResponse(User user, Chat chat, String[] args) {
        return TextTelegramMessageAdapter.builder()
                .chatId(chat.getId())
                .text(prepareStartText(user))
                .build();
    }

    private String prepareStartText(User user) {
        return String.format(
                """
                Добро пожаловать, %s! Этот бот умеет предсказывать валютные курсы, с помощью команды /rate.
                """,
                getUserFriendlyName(user)
        );
    }
}
