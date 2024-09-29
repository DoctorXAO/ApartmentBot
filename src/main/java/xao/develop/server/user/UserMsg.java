package xao.develop.server.user;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import xao.develop.config.BotConfig;
import xao.develop.config.UserCommand;
import xao.develop.config.UserMessageLink;
import xao.develop.server.BotMessage;
import xao.develop.server.MessageBuilder;
import xao.develop.server.Server;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.util.*;

@Slf4j
public abstract class UserMsg implements BotMessage, UserCommand, UserMessageLink {
    @Autowired
    BotConfig botConfig;

    @Autowired
    Server server;

    @Autowired
    MessageBuilder msgBuilder;

    @Override
    public Message sendMessage(Update update, String msgLink, Object... args) throws TelegramApiException {
        server.deleteOldMessages(update);

        return botConfig.getTelegramClient().execute(server.sendMessage(update,
                getFormatMessage(update, msgLink, args),
                getIKMarkup(update)));
    }

    @Override
    public void editMessage(Update update, List<Message> messages, String msgLink, Object... args) throws TelegramApiException {
        try {
            int lastMsgId = server.deleteAllMessagesExceptTheLastOne(update);

            log.debug("Method editMessage(Update, String): Last messageId {}", lastMsgId);

            botConfig.getTelegramClient().execute(server.editMessageText(update,
                    lastMsgId,
                    getFormatMessage(update, msgLink, args),
                    getIKMarkup(update)));
        } catch (TelegramApiException | IndexOutOfBoundsException ex) {
            log.warn("Method editMessage(Update, String) can't edit messageId. Exception: {}", ex.getMessage());
            messages.add(sendMessage(update, msgLink, args));
        }
    }

    private String getFormatMessage(Update update, String msgLink, Object... args) {
        try {
            return String.format(server.getLocaleMessage(update, msgLink), args);
        } catch (MissingFormatArgumentException ex) {
            log.warn("""
                    Impossible to format message link: {}
                    Exception: {}""", msgLink, ex.getMessage());
            return server.getLocaleMessage(update, msgLink);
        }
    }

    @Override
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
                    .chatId(server.getChatId(update))
                    .medias(photos)
                    .build());
        } catch (Exception ex) {
            log.error("sendPhotos: {}", ex.getMessage());

            return null;
        }
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
}
