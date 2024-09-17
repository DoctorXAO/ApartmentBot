package xao.develop.server.user;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import xao.develop.config.BotConfig;
import xao.develop.config.UserCommand;
import xao.develop.server.BotMessage;
import xao.develop.server.MessageBuilder;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
public class UserMessageApartment implements BotMessage, UserCommand {
    @Autowired
    BotConfig botConfig;

    @Autowired
    MessageBuilder msgBuilder;

    @Autowired
    UserLocalization userLoc;

    @Override
    public Message sendMessage(Update update) throws TelegramApiException {
        return botConfig.getTelegramClient().execute(msgBuilder.buildSendMessage(update,
                userLoc.getLocalizationText(update),
                getIKMarkup(update)));
    }

    public List<Message> testSendMessage(Update update) throws TelegramApiException {
        return botConfig.getTelegramClient().execute(sendPhotos(update, "apartment"));
    }

    public SendMediaGroup sendPhotos(Update update, String patch) {
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

            return SendMediaGroup
                    .builder()
                    .chatId(update.getCallbackQuery().getMessage().getChatId())
                    .medias(photos)
                    .build();
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

    @Override
    public InlineKeyboardMarkup getIKMarkup(Update update) {
        return InlineKeyboardMarkup
                .builder()
                .keyboardRow(new InlineKeyboardRow(msgBuilder.buildIKButton(
                        userLoc.getLocalizationButton(update, BACK), BACK_TO_START)))
                .build();
    }
}
