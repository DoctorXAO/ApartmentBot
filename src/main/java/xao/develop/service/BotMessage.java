package xao.develop.service;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import xao.develop.config.BotConfig;
import xao.develop.config.GeneralCommand;
import xao.develop.config.GeneralMessageLink;
import xao.develop.repository.Persistence;

import java.io.File;
import java.io.FilenameFilter;
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

    public void sendMessage(Update update, List<Integer> messages, String msgLink, Object... args) throws TelegramApiException {
        service.deleteOldMessages(update);

        messages.add(botConfig.getTelegramClient().execute(service.sendMessage(service.getChatId(update),
                service.getLocaleMessage(service.getChatId(update), msgLink, args),
                getIKMarkup(update))).getMessageId());
    }

    public void editMessage(Update update, List<Integer> messages, String msgLink, Object... args) throws TelegramApiException {
        try {
            int lastMsgId = service.deleteAllMessagesExceptTheLastOne(update);

            log.debug("Method editMessage(Update, String): Last messageId {}", lastMsgId);

            botConfig.getTelegramClient().execute(service.editMessageText(update,
                    lastMsgId,
                    service.getLocaleMessage(service.getChatId(update), msgLink, args),
                    getIKMarkup(update)));
        } catch (TelegramApiException | IndexOutOfBoundsException ex) {
            log.warn("Method editMessage(Update, String) can't edit messageId. Exception: {}", ex.getMessage());
            sendMessage(update, messages, msgLink, args);
        }
    }

    public void sendPhotos(Update update, List<Integer> messages, String patch) {
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            URL resource = classLoader.getResource(patch);

            if (resource == null)
                throw new Exception("Directory with photos isn't found!");

            List<InputMediaPhoto> photos = new ArrayList<>();

            File[] files = getSortedFiles(resource);

            for (File file : files)
                photos.add(new InputMediaPhoto(file, file.getName()));

            if (photos.size() == 1)
                throw new Exception("В процессе отправка одного фото..."); // todo доделать отправку одного фото
            else if (photos.size() < 2) // todo Можно сделать циклом по 10 фото отправлять
                throw new Exception("Current directory has less than two photos (*.jpg/*.png)");

            List<Message> msgPhotos = botConfig.getTelegramClient().execute(SendMediaGroup
                    .builder()
                    .chatId(service.getChatId(update))
                    .medias(photos)
                    .build());

            for (Message message : msgPhotos)
                messages.add(message.getMessageId());
        } catch (Exception ex) {
            log.error("sendPhotos: {}", ex.getMessage());
        }
    }

    protected InlineKeyboardMarkup getIKMarkup(Update update) {
        return null;
    }

    protected InlineKeyboardMarkup legacyGetIKMarkup(Update update, String msgLink, String data) {
        return InlineKeyboardMarkup
                .builder()
                .keyboardRow(new InlineKeyboardRow(msgBuilder.buildIKButton(
                        service.getLocaleMessage(service.getChatId(update), msgLink), data)))
                .build();
    }

    private @NotNull File[] getSortedFiles(URL resource) throws Exception {
        File directory = new File(resource.getFile());

        FilenameFilter filter = (dir, name) ->
                name.endsWith(".jpg") ||
                        name.endsWith(".jpeg") ||
                        name.endsWith(".png");

        File[] files = directory.listFiles(filter);

        if (!directory.isDirectory())
            throw new Exception("Указанный путь не является директорией!");
        else if (files == null)
            throw new Exception("В директории нет изображений с необходимым расширением (.jpg/.jpeg./.png)!");

        Arrays.sort(files, Comparator.comparing(File::getName));

        return files;
    }

    // legacy todo Переделать методы на более полезные

    public void legacySendMessage(Update update,
                                  List<Integer> messages,
                                  String msgLink,
                                  String btMsgLink,
                                  String btData,
                                  Object... args) throws TelegramApiException {
        service.deleteOldMessages(update);

        messages.add(botConfig.getTelegramClient().execute(service.sendMessage(service.getChatId(update),
                service.getLocaleMessage(service.getChatId(update), msgLink, args),
                legacyGetIKMarkup(update, btMsgLink, btData))).getMessageId());
    }

    public void legacyEditMessage(Update update,
                                  List<Integer> messages,
                                  String msgLink,
                                  String btMsgLink,
                                  String btData,
                                  Object... args) throws TelegramApiException {
        try {
            int lastMsgId = service.deleteAllMessagesExceptTheLastOne(update);

            log.debug("Method editMessage(Update, String, String, String, Object...): Last messageId {}", lastMsgId);

            botConfig.getTelegramClient().execute(service.editMessageText(update,
                    lastMsgId,
                    service.getLocaleMessage(service.getChatId(update), msgLink, args),
                    legacyGetIKMarkup(update, btMsgLink, btData)));
        } catch (TelegramApiException | IndexOutOfBoundsException ex) {
            log.warn("Method editMessage(Update, String, String, String, Object...) can't edit messageId. Exception: {}",
                    ex.getMessage());
            legacySendMessage(update, messages, msgLink, btMsgLink, btData, args);
        }
    }
}
