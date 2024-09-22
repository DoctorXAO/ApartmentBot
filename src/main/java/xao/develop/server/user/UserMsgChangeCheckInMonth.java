package xao.develop.server.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Slf4j
@Service
public class UserMsgChangeCheckInMonth extends UserMsg {

    @Autowired
    DateService dateS;

    static final int JANUARY = 0;
    static final int FEBRUARY = 1;
    static final int MARCH = 2;
    static final int APRIL = 3;
    static final int MAY = 4;
    static final int JUNE = 5;
    static final int JULY = 6;
    static final int AUGUST = 7;
    static final int SEPTEMBER = 8;
    static final int OCTOBER = 9;
    static final int NOVEMBER = 10;
    static final int DECEMBER = 11;

    @Override
    public InlineKeyboardMarkup getIKMarkup(Update update) {
        Calendar presentTime = dateS.getPresentTime();
        Calendar selectedTime = dateS.getSelectedTime(update);

        List<InlineKeyboardRow> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> buttons = new ArrayList<>();

        buttons.add(msgBuilder.buildIKButton(userLoc.getLocalizationButton(update, BACK),
                RAA_QUIT_FROM_CHANGE_CHECK_IN_MONTH));
        keyboard.add(msgBuilder.buildIKRow(buttons));
        buttons.clear();

        if (presentTime.get(Calendar.YEAR) < selectedTime.get(Calendar.YEAR))
            buttons.add(msgBuilder.buildIKButton("◀️", RAA_PREVIOUS_CHECK_IN_YEAR_CM));
        else
            buttons.add(msgBuilder.buildIKButton("🛑", EMPTY));

        buttons.add(msgBuilder.buildIKButton(dateS.getSelectedYear(selectedTime), RAA_CHANGE_CHECK_IN_YEAR));
        buttons.add(msgBuilder.buildIKButton("▶️", RAA_NEXT_CHECK_IN_YEAR_CM));
        keyboard.add(msgBuilder.buildIKRow(buttons));
        buttons.clear();

        log.debug("The present time is: {}", presentTime);
        log.debug("The selected time is: {}", selectedTime);

        for (int i = 1; i <= 12; i++) {
            selectedTime.set(Calendar.MONTH, i - 1); // -1 - так как класс Calendar ведет счет месяца с 0

            if (presentTime.get(Calendar.YEAR) < selectedTime.get(Calendar.YEAR))
                buttons.add(msgBuilder.buildIKButton(userLoc.getLocalizationButton(update, "month_" + i),
                        RAA_SET_MONTH + i));
            else if (presentTime.get(Calendar.MONTH) <= selectedTime.get(Calendar.MONTH))
                buttons.add(msgBuilder.buildIKButton(userLoc.getLocalizationButton(update, "month_" + i),
                        RAA_SET_MONTH + i));
            else
                buttons.add(msgBuilder.buildIKButton("🛑", EMPTY));

            keyboard.add(msgBuilder.buildIKRow(buttons));
            buttons.clear();
        }

        return InlineKeyboardMarkup
                .builder()
                .keyboard(keyboard)
                .build();
    }
}
