package ru.liga.rateprediction.telegram.controller;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Slf4j
public class RatePredictionBot extends TelegramLongPollingCommandBot {
    private final String botUserName;

    public RatePredictionBot(String botToken, String botUserName, List<IBotCommand> commands) {
        super(botToken);
        this.botUserName = botUserName;
        commands.forEach(iBotCommand -> {
            log.debug("Register command {} as {}", iBotCommand.getCommandIdentifier(), iBotCommand.getClass());
            register(iBotCommand);
        });
    }

    @Override
    public String getBotUsername() {
        return botUserName;
    }

    @Override
    public void processNonCommandUpdate(Update update) {
        Message msg = update.getMessage();
        Long chatId = msg.getChatId();
        String userName = getUserName(msg);

        String answer = "Unknown command";
        setAnswer(chatId, userName, answer);
    }

    private String getUserName(Message msg) {
        User user = msg.getFrom();
        String userName = user.getUserName();
        return (userName != null) ? userName : String.format("%s %s", user.getLastName(), user.getFirstName());
    }

    private void setAnswer(Long chatId, String userName, String text) {
        SendMessage answer = new SendMessage();
        answer.setText(text);
        answer.setChatId(chatId.toString());
        try {
            execute(answer);
        } catch (TelegramApiException e) {
            e.printStackTrace();
            //логируем сбой Telegram Bot API, используя userName
        }
    }
}
