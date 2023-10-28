package ru.liga.rateprediction.telegram.controller.commands;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.liga.rateprediction.telegram.controller.commands.adapt.TelegramMessageAdapter;
import ru.liga.rateprediction.telegram.controller.commands.adapt.TextTelegramMessageAdapter;

@Slf4j
public abstract class AbstractBotCommand extends BotCommand {
    /**
     * Construct a command
     *
     * @param commandIdentifier the unique identifier of this command (e.g. the command string to
     *                          enter into chat)
     * @param description       the description of this command
     */
    public AbstractBotCommand(String commandIdentifier, String description) {
        super(commandIdentifier, description);
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        final TelegramMessageAdapter adapter = prepareResponse(user, chat, arguments);
        try {
            adapter.execute(absSender);
        } catch (TelegramApiException e) {
            log.warn("Can't execute command of {} cause of: {}", getClass(), e.getMessage(), e);
            try {
                final SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(chat.getId());
                sendMessage.setText(String.format("Unexpected error occurred: %s", e.getMessage()));
                absSender.execute(sendMessage);
            } catch (TelegramApiException ex) {
                log.error("Can't execute recover message, due: {}", ex.getMessage(), ex);
            }
        }
    }

    protected String getUserFriendlyName(User user) {
        return user.getFirstName() + user.getLastName();
    }

    protected abstract TelegramMessageAdapter prepareResponse(User user, Chat chat, String[] args);
}
