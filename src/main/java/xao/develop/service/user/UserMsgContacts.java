package xao.develop.service.user;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

@Service
public class UserMsgContacts extends UserMessage {

    @Override
    public InlineKeyboardMarkup getIKMarkup(Update update) {
        return InlineKeyboardMarkup
                .builder()
                .keyboardRow(new InlineKeyboardRow(msgBuilder.buildIKButton(
                        service.getLocaleMessage(update, USER_BT_BACK), BACK_TO_START)))
                .build();
    }
}