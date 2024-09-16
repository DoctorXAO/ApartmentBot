package xao.develop.server;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.List;

@Slf4j
@Service
public class MessageBuilder {
    @Autowired
    Server server;

    public InlineKeyboardRow buildIKRow(List<InlineKeyboardButton> buttons) {
        return new InlineKeyboardRow(buttons);
    }

    public InlineKeyboardButton buildIKButton(String text, String callbackData) {
        return InlineKeyboardButton
                .builder()
                .text(text)
                .callbackData(callbackData)
                .build();
    }

    public SendMessage buildSendMessage(Update update, String text, InlineKeyboardMarkup markup) {
        return SendMessage
                .builder()
                .chatId(server.getChatId(update))
                .text(text)
                .replyMarkup(markup)
                .parseMode("HTML")
                .build();
    }
}
