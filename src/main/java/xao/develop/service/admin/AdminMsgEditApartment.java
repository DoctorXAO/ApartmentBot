package xao.develop.service.admin;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.ArrayList;
import java.util.List;

@Service
public class AdminMsgEditApartment extends AdminMessage {

    @Override
    protected InlineKeyboardMarkup getIKMarkup(long chatId) {
        List<InlineKeyboardRow> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> buttons = new ArrayList<>();

        buttons.add(msgBuilder.buildIKButton(service.getLocaleMessage(chatId, ADMIN_BT_EDIT_PHOTOS), OPEN_EDIT_PHOTOS));
        keyboard.add(msgBuilder.buildIKRow(buttons));
        buttons.clear();

        buttons.add(msgBuilder.buildIKButton(service.getLocaleMessage(chatId, ADMIN_BT_EDIT_NUMBER), OPEN_EDIT_NUMBER));
        keyboard.add(msgBuilder.buildIKRow(buttons));
        buttons.clear();

        buttons.add(msgBuilder.buildIKButton(service.getLocaleMessage(chatId, ADMIN_BT_EDIT_AREA), OPEN_EDIT_AREA));
        keyboard.add(msgBuilder.buildIKRow(buttons));
        buttons.clear();

        buttons.add(msgBuilder.buildIKButton(service.getLocaleMessage(chatId, ADMIN_BT_EDIT_AMENITIES), OPEN_EDIT_AMENITIES));
        keyboard.add(msgBuilder.buildIKRow(buttons));
        buttons.clear();

        buttons.add(msgBuilder.buildIKButton(service.getLocaleMessage(chatId, ADMIN_BT_DELETE), APPLY_DELETE_APARTMENT));
        keyboard.add(msgBuilder.buildIKRow(buttons));
        buttons.clear();

        buttons.add(msgBuilder.buildIKButton(service.getLocaleMessage(chatId, GENERAL_BT_BACK), LIST_OF_APARTMENTS));
        keyboard.add(msgBuilder.buildIKRow(buttons));

        return InlineKeyboardMarkup
                .builder()
                .keyboard(keyboard)
                .build();
    }
}
