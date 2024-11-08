package xao.develop.service.user;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

@Service
public class UserMsgAboutUs extends UserMessage {

    @Override
    public InlineKeyboardMarkup getIKMarkup(long chatId) {
        return InlineKeyboardMarkup
                .builder()
                .keyboardRow(new InlineKeyboardRow(msgBuilder.buildIKButton(
                        service.getLocaleMessage(chatId, GENERAL_BT_BACK), BACK_TO_START)))
                .build();
    }
}
