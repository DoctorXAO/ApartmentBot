package xao.develop.service.admin;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class AdminMsgOpenApp extends AdminMessage {

    @Override
    protected InlineKeyboardMarkup getIKMarkup(Update update) {
        List<InlineKeyboardRow> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> buttons = new ArrayList<>();

        int selectedApp = persistence.selectTempAdminSettings(service.getChatId(update)).getSelectedApplication();

        buttons.add(msgBuilder.buildIKButton(service.getLocaleMessage(update, ADMIN_BT_REFUSE),
                REFUSE_APP + X + selectedApp));
        buttons.add(msgBuilder.buildIKButton(service.getLocaleMessage(update, ADMIN_BT_ACCEPT),
                ACCEPT_APP + X + selectedApp));
        keyboard.add(msgBuilder.buildIKRow(buttons));
        buttons.clear();

        initBtChat(update, keyboard, buttons);

        buttons.add(msgBuilder.buildIKButton(service.getLocaleMessage(update, ADMIN_BT_BACK), QUIT_FROM_APP));
        keyboard.add(msgBuilder.buildIKRow(buttons));
        buttons.clear();

        return InlineKeyboardMarkup
                .builder()
                .keyboard(keyboard)
                .build();
    }
}
