package xao.develop.service.user;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Service
public class UserMsgChangeCheckYear extends UserDate {

    @Override
    public InlineKeyboardMarkup getIKMarkup(Update update) {
        Calendar presentTime = getPresentTime(update);

        List<InlineKeyboardRow> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> buttons = new ArrayList<>();

        buttons.add(msgBuilder.buildIKButton(service.getLocaleMessage(update, USER_BT_BACK),
                RAA_QUIT_FROM_CHANGE_CHECK_MONTH));
        keyboard.add(msgBuilder.buildIKRow(buttons));
        buttons.clear();

        for (int i = 0; i <= MAX_YEAR; i++) {
            String year = String.valueOf(presentTime.get(Calendar.YEAR) + i);
            buttons.add(msgBuilder.buildIKButton(year, RAA_SET_YEAR + year));

            keyboard.add(msgBuilder.buildIKRow(buttons));
            buttons.clear();
        }

        return InlineKeyboardMarkup
                .builder()
                .keyboard(keyboard)
                .build();
    }
}
