package xao.develop.service.admin;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.ArrayList;
import java.util.List;

@Service
public class AdminMsgEditPhotos extends AdminMessage {

    @Override
    protected InlineKeyboardMarkup getIKMarkup(long chatId) {
        List<InlineKeyboardRow> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> buttons = new ArrayList<>();

        buttons.add(msgBuilder.buildIKButton(service.getLocaleMessage(chatId, ADMIN_BT_ADD_PHOTOS), ADD_PHOTOS));
        keyboard.add(msgBuilder.buildIKRow(buttons));
        buttons.clear();

        buttons.add(msgBuilder.buildIKButton(service.getLocaleMessage(chatId, ADMIN_BT_REPLACE_PHOTOS), REPLACE_PHOTOS));
        keyboard.add(msgBuilder.buildIKRow(buttons));
        buttons.clear();

        buttons.add(msgBuilder.buildIKButton(service.getLocaleMessage(chatId, GENERAL_BT_BACK),
                APARTMENT + X + getSelectedApartment(chatId)));
        keyboard.add(msgBuilder.buildIKRow(buttons));

        return InlineKeyboardMarkup
                .builder()
                .keyboard(keyboard)
                .build();
    }
}