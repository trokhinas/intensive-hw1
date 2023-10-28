package ru.liga.rateprediction.telegram.controller.commands.adapt;

import lombok.Builder;
import lombok.Value;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.ByteArrayInputStream;
import java.util.Optional;

@Value
@Builder
public class ImageTelegramMessageAdapterImpl implements TelegramMessageAdapter {
    Long chatId;

    byte[] image;

    String filename;

    @Override
    public void execute(AbsSender absSender) throws TelegramApiException {
        final SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setPhoto(createPhoto());
        sendPhoto.setChatId(chatId);

        absSender.execute(sendPhoto);
    }

    private InputFile createPhoto() {
        final String filename = Optional.ofNullable(this.filename)
                .filter(x -> !x.isBlank())
                .orElse("image.png");

        return new InputFile(new ByteArrayInputStream(image), filename);
    }
}
