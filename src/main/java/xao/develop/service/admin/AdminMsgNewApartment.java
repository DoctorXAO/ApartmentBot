package xao.develop.service.admin;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.ArrayList;
import java.util.List;

@Service
public class AdminMsgNewApartment extends AdminMessage {

    @Override
    protected InlineKeyboardMarkup getIKMarkup(long chatId) {
        List<InlineKeyboardRow> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> buttons = new ArrayList<>();

        initBtPreview(chatId, keyboard, buttons);

        buttons.add(msgBuilder.buildIKButton(service.getLocaleMessage(chatId, ADMIN_BT_CANCEL), QUIT_TO_SETTINGS));
        keyboard.add(msgBuilder.buildIKRow(buttons));

        return InlineKeyboardMarkup
                .builder()
                .keyboard(keyboard)
                .build();
    }
}
