package xao.develop.server.user;

import lombok.extern.slf4j.Slf4j;
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
public class UserMsgChooseCheckDate extends UserDate {

    @Override
    public InlineKeyboardMarkup getIKMarkup(Update update) {
        List<InlineKeyboardRow> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> buttons = new ArrayList<>();

        Calendar presentTime = getPresentTime(update);
        Calendar selectedTime = getSelectedTime(update);

        int maxPresentDaysOfMonth = getMaxDaysOfMonth(selectedTime, 0);
        int firstDayOfWeekInMonth = getFirstDayOfWeekInMonth(selectedTime);
        int presentDayOfMonth = getPresentDayOfMonth(presentTime);

        initHeaderIKMarkup(update, keyboard, buttons, presentTime, selectedTime);

        // Добавляет недостающие дни недели из последних дней предыдущего месяца
        if (firstDayOfWeekInMonth != 1)
            for (int i = firstDayOfWeekInMonth - 2; i >= 0; i--)
                buttons.add(msgBuilder.buildIKButton("🛑", EMPTY));

        boolean areDatesEquals = checkEqualsDate(update);

        log.debug("areDatesEquals = {}", areDatesEquals);

        // Добавляет все дни выбранного месяца по неделям
        for (int i = 1; i <= maxPresentDaysOfMonth; i++) {

            if (i >= presentDayOfMonth || !areDatesEquals)
                buttons.add(msgBuilder.buildIKButton(String.valueOf(i), RAA_SET_DAY + i));
            else
                buttons.add(msgBuilder.buildIKButton("🛑", EMPTY));

            // Добавляет все недостающие дни недели из первых дней следующего месяца
            if (i == maxPresentDaysOfMonth && getDayOfWeekInMonth(selectedTime, i) != 7) {
                int difference = 7 - getDayOfWeekInMonth(selectedTime, i);
                for (int j = 1; j <= difference; j++)
                    buttons.add(msgBuilder.buildIKButton("🛑", EMPTY));
                keyboard.add(msgBuilder.buildIKRow(buttons));
                buttons.clear();
                break;
            }

            // Если последний день недели, то оборвать линию и начать новую
            if (getDayOfWeekInMonth(selectedTime, i) == 7) {
                keyboard.add(msgBuilder.buildIKRow(buttons));
                buttons.clear();
            }
        }

        buttons.add(msgBuilder.buildIKButton(userLoc.getLocalizationButton(update, BACK), RAA_QUIT_FROM_CHOOSER_CHECK));
        keyboard.add(msgBuilder.buildIKRow(buttons));

        return InlineKeyboardMarkup
                .builder()
                .keyboard(keyboard)
                .build();
    }

    private void initHeaderIKMarkup(Update update,
                                    List<InlineKeyboardRow> keyboard,
                                    List<InlineKeyboardButton> buttons,
                                    Calendar today,
                                    Calendar calendar) {

        if (today.get(Calendar.YEAR) < calendar.get(Calendar.YEAR))
            buttons.add(msgBuilder.buildIKButton("◀️", RAA_PREVIOUS_CHECK_YEAR));
        else
            buttons.add(msgBuilder.buildIKButton("🛑", EMPTY));

        buttons.add(msgBuilder.buildIKButton(getSelectedYear(calendar), RAA_CHANGE_CHECK_IN_YEAR));
        buttons.add(msgBuilder.buildIKButton("▶️", RAA_NEXT_CHECK_YEAR));
        keyboard.add(msgBuilder.buildIKRow(buttons));
        buttons.clear();

        if (today.get(Calendar.MONTH) < calendar.get(Calendar.MONTH) || today.get(Calendar.YEAR) < calendar.get(Calendar.YEAR))
            buttons.add(msgBuilder.buildIKButton("◀️", RAA_PREVIOUS_CHECK_MONTH));
        else
            buttons.add(msgBuilder.buildIKButton("🛑", EMPTY));

        buttons.add(msgBuilder.buildIKButton(
                userLoc.getLocalizationButton(update, "month_" + (calendar.get(Calendar.MONTH) + 1)),
                RAA_CHANGE_CHECK_IN_MONTH));
        buttons.add(msgBuilder.buildIKButton("▶️", RAA_NEXT_CHECK_MONTH));
        keyboard.add(msgBuilder.buildIKRow(buttons));
        buttons.clear();
    }
}
