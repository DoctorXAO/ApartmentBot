package xao.develop.server;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public interface BotMessage {
    Message sendMessage(Update update) throws TelegramApiException;

    InlineKeyboardMarkup getIKMarkup(Update update);
}
