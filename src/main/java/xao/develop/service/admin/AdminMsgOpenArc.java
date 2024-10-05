package xao.develop.service.admin;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import xao.develop.config.enums.TypeOfAppStatus;

import java.util.ArrayList;
import java.util.List;

@Service
public class AdminMsgOpenArc extends AdminMessage {

    @Override
    protected InlineKeyboardMarkup getIKMarkup(Update update) {
        List<InlineKeyboardRow> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> buttons = new ArrayList<>();

        int selectedApp = persistence.selectTempAdminSettings(service.getChatId(update)).getSelectedApplication();

        if (!getStatusOfApp(selectedApp).equals(TypeOfAppStatus.FINISHED.getType())) {
            buttons.add(msgBuilder.buildIKButton(service.getLocaleMessage(service.getChatId(update), ADMIN_BT_RETURN),
                    RETURN_APP + X + selectedApp));
            keyboard.add(msgBuilder.buildIKRow(buttons));
            buttons.clear();
        }

        initBtChat(update, keyboard, buttons);

        buttons.add(msgBuilder.buildIKButton(service.getLocaleMessage(service.getChatId(update), GENERAL_BT_BACK), QUIT_FROM_ARC));
        keyboard.add(msgBuilder.buildIKRow(buttons));
        buttons.clear();

        return InlineKeyboardMarkup
                .builder()
                .keyboard(keyboard)
                .build();
    }
}
