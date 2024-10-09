package xao.develop.toolbox;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
public class TelegramFileManager {

    public static String getFilePath(@NotNull OkHttpTelegramClient client, @NotNull String fileId) {
        try {
            return client.execute(new GetFile(fileId)).getFilePath();
        } catch (TelegramApiException ex) {
            log.warn("Can't get the path to the photo. Exception: {}", ex.getMessage());
            return null;
        }
    }
}
