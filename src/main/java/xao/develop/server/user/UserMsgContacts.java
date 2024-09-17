package xao.develop.server.user;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Service
public class UserMsgContacts extends UserMsg {

    @Override
    public Message sendMessage(Update update) throws TelegramApiException {
        return botConfig.getTelegramClient().execute(msgBuilder.buildSendMessage(update,
                String.format(userLoc.getLocalizationText(update), botConfig.getPhone(), botConfig.getEmail()),
                getIKMarkup(update)));
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
