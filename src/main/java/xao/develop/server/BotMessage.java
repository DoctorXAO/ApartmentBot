package xao.develop.server;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

public interface BotMessage {

    Message sendMessage(Update update, String msgLink, Object... args) throws TelegramApiException;

    void editMessage(Update update, List<Message> messages, String msgLink, Object... args) throws TelegramApiException;

    List<Message> sendPhotos(Update update, String patch);

    InlineKeyboardMarkup getIKMarkup(Update update);
}
