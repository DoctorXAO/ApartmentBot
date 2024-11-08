package xao.develop.service.user;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserMsgNoFreeApartment extends UserMessage {

    @Override
    protected InlineKeyboardMarkup getIKMarkup(long chatId) {
        List<InlineKeyboardRow> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> buttons = new ArrayList<>();

        buttons.add(msgBuilder.buildIKButton(service.getLocaleMessage(chatId, GENERAL_BT_BACK), RAA_QUIT_FROM_CHOOSER_AN_APARTMENT));
        keyboard.add(msgBuilder.buildIKRow(buttons));

        return InlineKeyboardMarkup
                .builder()
                .keyboard(keyboard)
                .build();
    }
}
