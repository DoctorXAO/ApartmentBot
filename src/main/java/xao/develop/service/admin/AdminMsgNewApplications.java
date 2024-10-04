package xao.develop.service.admin;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import xao.develop.config.enums.TypeOfApp;

import java.util.ArrayList;
import java.util.List;

@Service
public class AdminMsgNewApplications extends AdminMessage {

    @Override
    protected InlineKeyboardMarkup getIKMarkup(Update update) {
        List<InlineKeyboardRow> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> buttons = new ArrayList<>();

        initSelectorApps(update, keyboard, buttons, TypeOfApp.APP);

        buttons.add(msgBuilder.buildIKButton(service.getLocaleMessage(update, ADMIN_BT_BACK), BACK_TO_START));
        keyboard.add(msgBuilder.buildIKRow(buttons));
        buttons.clear();

        return InlineKeyboardMarkup
                .builder()
                .keyboard(keyboard)
                .build();
    }
}
