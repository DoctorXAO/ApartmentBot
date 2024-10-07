package xao.develop.service.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Slf4j
@Service
public class UserMsgChangeCheckMonth extends UserDate {

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
    public InlineKeyboardMarkup getIKMarkup(long chatId) {
        Calendar presentTime = getPresentTime(chatId);
        Calendar selectedTime = getSelectedTime(chatId);

        List<InlineKeyboardRow> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> buttons = new ArrayList<>();

        buttons.add(msgBuilder.buildIKButton(service.getLocaleMessage(chatId, GENERAL_BT_BACK),
                QUIT_FROM_CHANGE_CHECK_MONTH));
        keyboard.add(msgBuilder.buildIKRow(buttons));
        buttons.clear();

        int pYear = presentTime.get(Calendar.YEAR);
        int pMonth = presentTime.get(Calendar.MONTH);

        Calendar maxCalendar = (Calendar) presentTime.clone();
        maxCalendar.set(Calendar.YEAR, maxCalendar.get(Calendar.YEAR) + MAX_YEAR);

        int mYear = maxCalendar.get(Calendar.YEAR);
        int mMonth = maxCalendar.get(Calendar.MONTH);

        if (pYear < selectedTime.get(Calendar.YEAR))
            buttons.add(msgBuilder.buildIKButton("◀️", PREVIOUS_CHECK_YEAR_CM));
        else
            buttons.add(msgBuilder.buildIKButton("🛑", EMPTY));

        buttons.add(msgBuilder.buildIKButton(getSelectedYear(selectedTime), CHANGE_CHECK_YEAR));

        if (selectedTime.get(Calendar.YEAR) < mYear)
            buttons.add(msgBuilder.buildIKButton("▶️", NEXT_CHECK_YEAR_CM));
        else
            buttons.add(msgBuilder.buildIKButton("🛑", EMPTY));

        keyboard.add(msgBuilder.buildIKRow(buttons));
        buttons.clear();

        log.debug("The present time is: {}", presentTime);
        log.debug("The selected time is: {}", selectedTime);

        for (int i = 1; i <= 12; i++) {
            selectedTime.set(Calendar.MONTH, i - 1); // -1 - так как класс Calendar ведет счет месяца с 0

            int sYear = selectedTime.get(Calendar.YEAR);
            int sMonth = selectedTime.get(Calendar.MONTH);

            if (sYear > mYear || (sYear == mYear && sMonth > mMonth))
                buttons.add(msgBuilder.buildIKButton("🛑", EMPTY));
            else if (sYear > pYear || sMonth >= pMonth)
                buttons.add(msgBuilder.buildIKButton(service.getLocaleMessage(chatId, USER_BT_MONTH_ + i),
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
