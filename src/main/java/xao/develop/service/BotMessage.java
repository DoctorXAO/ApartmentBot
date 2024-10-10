package xao.develop.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import xao.develop.config.BotConfig;
import xao.develop.command.GeneralCommand;
import xao.develop.command.GeneralMessageLink;
import xao.develop.repository.Persistence;
import xao.develop.toolbox.FileManager;

import java.io.File;
import java.net.URL;
import java.util.*;

@Slf4j
public abstract class BotMessage implements GeneralMessageLink, GeneralCommand {

    @Autowired
    protected BotConfig botConfig;

    @Autowired
    protected BotService service;

    @Autowired
    protected MessageBuilder msgBuilder;

    @Autowired
    protected Persistence persistence;

    // setters

    // getters

    public String getStatusIcon(String status) {
        switch (status) {
            case WAITING -> status = "\uD83C\uDF00";
            case ACCEPTED -> status = "✅";
            case DENIED -> status = "❌";
            case FINISHED -> status = "\uD83C\uDFC1";
            default -> status = "null";
        }

        return status;
    }

    // actions

    public int sendMessage(long chatId, String msgLink, Object... args) throws TelegramApiException {
        service.deleteOldMessages(chatId);

        return botConfig.getTelegramClient().execute(service.sendMessage(chatId,
                service.getLocaleMessage(chatId, msgLink, args),
                getIKMarkup(chatId))).getMessageId();
    }

    public int editMessage(long chatId, String msgLink, Object... args) throws TelegramApiException {
        try {
            int lastMsgId = service.deleteAllMessagesExceptTheLastOne(chatId);

            log.debug("Method editMessage(long, String): Last messageId {}", lastMsgId);

            botConfig.getTelegramClient().execute(service.editMessageText(chatId,
                    lastMsgId,
                    service.getLocaleMessage(chatId, msgLink, args),
                    getIKMarkup(chatId)));

            return 0;
        } catch (TelegramApiException | IndexOutOfBoundsException ex) {
            log.warn("Method editMessage(long, String) can't edit messageId. Exception: {}", ex.getMessage());
            return sendMessage(chatId, msgLink, args);
        }
    }

    public List<Integer> sendPhotos(long chatId, String path) {
        try {
            if (path == null)
                throw new Exception("Directory with photos isn't found!");

            List<InputMediaPhoto> photos = new ArrayList<>();

            File[] files = FileManager.getSortedFiles(new URL(path));

            for (File file : files)
                photos.add(new InputMediaPhoto(file, file.getName()));

            if (photos.size() == 1)
                throw new Exception("В процессе отправка одного фото..."); // todo доделать отправку одного фото
            else if (photos.size() < 2) // todo Можно сделать циклом по 10 фото отправлять
                throw new Exception("Current directory has less than two photos (*.jpg/*.png)");

            List<Message> msgPhotos = botConfig.getTelegramClient().execute(SendMediaGroup
                    .builder()
                    .chatId(chatId)
                    .medias(photos)
                    .build());

            List<Integer> messages = new ArrayList<>();

            for (Message message : msgPhotos)
                messages.add(message.getMessageId());

            return messages;
        } catch (Exception ex) {
            log.error("Impossible send photos. Path: {}\nException: {}", path, ex.getMessage());
            return new ArrayList<>();
        }
    }

    abstract protected InlineKeyboardMarkup getIKMarkup(long chatId);

    // markups

    public InlineKeyboardMarkup getIKMarkupOkToDelete(long chatId) {
        List<InlineKeyboardRow> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> buttons = new ArrayList<>();

        buttons.add(msgBuilder.buildIKButton(service.getLocaleMessage(chatId, GENERAL_BT_OK), DELETE));
        keyboard.add(msgBuilder.buildIKRow(buttons));

        return InlineKeyboardMarkup
                .builder()
                .keyboard(keyboard)
                .build();
    }

    public InlineKeyboardMarkup getIKMarkupChat(long chatId) {
        List<InlineKeyboardRow> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> buttons = new ArrayList<>();

        buttons.add(msgBuilder.buildIKButton(service.getLocaleMessage(chatId, GENERAL_BT_OK), DELETE));
        keyboard.add(msgBuilder.buildIKRow(buttons));

        return InlineKeyboardMarkup
                .builder()
                .keyboard(keyboard)
                .build();
    }
}
