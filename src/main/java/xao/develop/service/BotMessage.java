package xao.develop.service;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import xao.develop.config.BotConfig;
import xao.develop.command.GeneralCommand;
import xao.develop.command.GeneralMessageLink;
import xao.develop.repository.AmenityPersistence;
import xao.develop.repository.Persistence;
import xao.develop.repository.TempNewAmenityPersistence;
import xao.develop.toolbox.FileManager;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
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

    @Autowired
    protected AmenityPersistence amenityPersistence;

    @Autowired
    protected TempNewAmenityPersistence tempNewAmenityPersistence;

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

    public List<Integer> sendPhotos(long chatId, @NotNull String path) {
        try {
            File[] files = FileManager.getSortedFiles(new URL(path));

            if (files.length == 1)
                return sendSinglePhoto(chatId, new InputFile(files[0]));
            else {
                return sendSomePhotos(chatId,
                        new ArrayList<>(Arrays.stream(files)
                        .map(file -> new InputMediaPhoto(file, file.getName()))
                        .toList()));
            }
        } catch (MalformedURLException ex) {
            log.error("Can't download URL.\nException: {}", ex.getMessage());

            ex.printStackTrace();

            return new ArrayList<>();
        } catch (IOException ex) {
            log.error("Can't sort files.\nException: {}", ex.getMessage());

            return new ArrayList<>();
        }
    }

    private List<Integer> sendSinglePhoto(long chatId, InputFile photo) {
        try {
            List<Integer> photoId = new ArrayList<>();

            photoId.add(botConfig.getTelegramClient().execute(SendPhoto
                    .builder()
                    .chatId(chatId)
                    .photo(photo)
                    .build()).getMessageId());

            return photoId;
        } catch (TelegramApiException ex) {
            log.error("Can't send the single photo.\nException: {}", ex.getMessage());

            return new ArrayList<>();
        }
    }

    private List<Integer> sendSomePhotos(long chatId, List<InputMediaPhoto> photos) {
        try {
            List<Integer> msgIds = new ArrayList<>();
            List<InputMediaPhoto> temp = new ArrayList<>();

            for(InputMediaPhoto photo : photos) {
                if (temp.size() >= 10) {
                    msgIds.addAll(executeSomePhotos(chatId, temp));

                    temp.clear();
                }

                temp.add(photo);
            }

            if (!temp.isEmpty())
                msgIds.addAll(executeSomePhotos(chatId, temp));

            return msgIds;
        } catch (TelegramApiException ex) {
            log.error("Can't send some photos.\nException: {}", ex.getMessage());

            return new ArrayList<>();
        }
    }

    private List<Integer> executeSomePhotos(long chatId, List<InputMediaPhoto> photos) throws TelegramApiException {
        return botConfig.getTelegramClient().execute(SendMediaGroup
                .builder()
                .chatId(chatId)
                .medias(photos)
                .build()).stream().map(Message::getMessageId).toList();
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
