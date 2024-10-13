package xao.develop.service.admin;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import xao.develop.enums.AppStatus;

import java.util.ArrayList;
import java.util.List;

@Service
public class AdminMsgOpenArc extends AdminMessage {

    @Override
    protected InlineKeyboardMarkup getIKMarkup(long chatId) {
        List<InlineKeyboardRow> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> buttons = new ArrayList<>();

        int selectedApp = persistence.selectAdminSettings(chatId).getSelectedApplication();

        if (!getStatusOfApp(selectedApp).equals(AppStatus.FINISHED.getType())) {
            buttons.add(msgBuilder.buildIKButton(service.getLocaleMessage(chatId, ADMIN_BT_RETURN),
                    RETURN_APP + X + selectedApp));
            keyboard.add(msgBuilder.buildIKRow(buttons));
            buttons.clear();
        }

        buttons.add(msgBuilder.buildIKButton(service.getLocaleMessage(chatId, GENERAL_BT_BACK), QUIT_FROM_ARC));
        keyboard.add(msgBuilder.buildIKRow(buttons));
        buttons.clear();

        return InlineKeyboardMarkup
                .builder()
                .keyboard(keyboard)
                .build();
    }
}
