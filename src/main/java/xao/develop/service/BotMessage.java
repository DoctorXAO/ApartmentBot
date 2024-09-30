package xao.develop.service;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import xao.develop.config.BotConfig;
import xao.develop.repository.Persistence;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.util.*;

@Slf4j
public abstract class BotMessage {

    @Autowired
    protected BotConfig botConfig;

    @Autowired
    protected BotService service;

    @Autowired
    protected MessageBuilder msgBuilder;

    @Autowired
    protected Persistence persistence;

    public Message sendMessage(Update update, String msgLink, Object... args) throws TelegramApiException {
        service.deleteOldMessages(update);

        return botConfig.getTelegramClient().execute(service.sendMessage(update,
                service.getLocaleMessage(update, msgLink, args),
                getIKMarkup(update)));
    }

    public void editMessage(Update update, List<Message> messages, String msgLink, Object... args) throws TelegramApiException {
        try {
            int lastMsgId = service.deleteAllMessagesExceptTheLastOne(update);

            log.debug("Method editMessage(Update, String): Last messageId {}", lastMsgId);

            botConfig.getTelegramClient().execute(service.editMessageText(update,
                    lastMsgId,
                    service.getLocaleMessage(update, msgLink, args),
                    getIKMarkup(update)));
        } catch (TelegramApiException | IndexOutOfBoundsException ex) {
            log.warn("Method editMessage(Update, String) can't edit messageId. Exception: {}", ex.getMessage());
            messages.add(sendMessage(update, msgLink, args));
        }
    }

    public List<Message> sendPhotos(Update update, String patch) {
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
            else if (photos.size() < 2)
                throw new Exception("Current directory has less than two photos (*.jpg/*.png)");

            return botConfig.getTelegramClient().execute(SendMediaGroup
                    .builder()
                    .chatId(service.getChatId(update))
                    .medias(photos)
                    .build());
        } catch (Exception ex) {
            log.error("sendPhotos: {}", ex.getMessage());

            return null;
        }
    }

    abstract protected InlineKeyboardMarkup getIKMarkup(Update update);

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
}
