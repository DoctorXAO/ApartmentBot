package xao.develop.server.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import xao.develop.repository.Persistence;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Slf4j
@Service
public class UserMsgChooseCheckInDate extends UserMsg {

    @Autowired
    Persistence persistence;

    @Autowired
    DateService dateS;

    void addNewUserToTempBookingData(Update update) {
        persistence.insertTempBookingData(server.getChatId(update), Calendar.getInstance().getTimeInMillis());
    }

    void nextMonth(Update update) {
        Calendar selectedTime = Calendar.getInstance();
        selectedTime.setTimeInMillis(persistence.selectTempBookingData(server.getChatId(update)).getSelectedTime());

        selectedTime.set(Calendar.MONTH, selectedTime.get(Calendar.MONTH) + 1);

        persistence.updateTempBookingData(server.getChatId(update), selectedTime.getTimeInMillis());
    }

    void previousMonth(Update update) {
        Calendar selectedTime = Calendar.getInstance();
        selectedTime.setTimeInMillis(persistence.selectTempBookingData(server.getChatId(update)).getSelectedTime());

        selectedTime.set(Calendar.MONTH, selectedTime.get(Calendar.MONTH) - 1);

        persistence.updateTempBookingData(server.getChatId(update), selectedTime.getTimeInMillis());
    }

    void nextYear(Update update) {
        Calendar selectedTime = dateS.getSelectedTime(update);

        log.debug("Selected time before: {}", selectedTime);

        selectedTime.set(Calendar.YEAR, selectedTime.get(Calendar.YEAR) + 1);

        log.debug("Selected time after: {}", selectedTime);

        persistence.updateTempBookingData(server.getChatId(update), selectedTime.getTimeInMillis());
    }

    void previousYear(Update update) {
        Calendar selectedTime = dateS.getSelectedTime(update);

        selectedTime.set(Calendar.YEAR, selectedTime.get(Calendar.YEAR) - 1);

        persistence.updateTempBookingData(server.getChatId(update),
                Math.max(dateS.getPresentTime().getTimeInMillis(), selectedTime.getTimeInMillis()));
    }

    boolean checkEqualsDate(Update update) {
        Calendar presentTime = dateS.getPresentTime();
        Calendar selectedTime = dateS.getSelectedTime(update);

        boolean areMonthsEqual = presentTime.get(Calendar.MONTH) == selectedTime.get(Calendar.MONTH);
        boolean areYearsEqual = presentTime.get(Calendar.YEAR) == selectedTime.get(Calendar.YEAR);

        return areMonthsEqual && areYearsEqual;
    }

    void setSelectedMonth(Update update, int month) {
        log.debug("Selected number of month: {}", month);

        if (month >= 0 && month <= 11) {
            Calendar selectedTime = dateS.getSelectedTime(update);
            selectedTime.set(Calendar.MONTH, month);
            persistence.updateTempBookingData(server.getChatId(update), selectedTime.getTimeInMillis());
        } else
            log.warn("Unknown number of month: {}", month);
    }

    @Override
    public InlineKeyboardMarkup getIKMarkup(Update update) {
        List<InlineKeyboardRow> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> buttons = new ArrayList<>();

        Calendar presentTime = dateS.getPresentTime();
        Calendar selectedTime = dateS.getSelectedTime(update);

        int maxCurrentDaysOfMonth = dateS.getMaxDaysOfMonth(selectedTime, 0);
        int firstDayOfWeekInMonth = dateS.getFirstDayOfWeekInMonth(selectedTime);
        int currentDayOfMonth = dateS.getCurrentDayOfMonth(presentTime);

        initHeaderIKMarkup(update, keyboard, buttons, presentTime, selectedTime);

        // Добавляет недостающие дни недели из последних дней предыдущего месяца
        if (firstDayOfWeekInMonth != 1)
            for (int i = firstDayOfWeekInMonth - 2; i >= 0; i--)
                buttons.add(msgBuilder.buildIKButton("🛑", EMPTY));

        boolean areDatesEquals = checkEqualsDate(update);

        log.debug("areDatesEquals = {}", areDatesEquals);

        // Добавляет все дни выбранного месяца по неделям
        for (int i = 1; i <= maxCurrentDaysOfMonth; i++) {

            if (i >= currentDayOfMonth && areDatesEquals)
                buttons.add(msgBuilder.buildIKButton(String.valueOf(i), DAYS + i));
            else if (!areDatesEquals)
                buttons.add(msgBuilder.buildIKButton(String.valueOf(i), DAYS + i));
            else
                buttons.add(msgBuilder.buildIKButton("🛑", EMPTY));

            // Добавляет все недостающие дни недели из первых дней следующего месяца
            if (i == maxCurrentDaysOfMonth && dateS.getDayOfWeekInMonth(selectedTime, i) != 7) {
                int difference = 7 - dateS.getDayOfWeekInMonth(selectedTime, i);
                for (int j = 1; j <= difference; j++)
                    buttons.add(msgBuilder.buildIKButton("🛑", EMPTY));
                keyboard.add(msgBuilder.buildIKRow(buttons));
                buttons.clear();
                break;
            }

            // Если последний день недели, то оборвать линию и начать новую
            if (dateS.getDayOfWeekInMonth(selectedTime, i) == 7) {
                keyboard.add(msgBuilder.buildIKRow(buttons));
                buttons.clear();
            }
        }

        buttons.add(msgBuilder.buildIKButton(userLoc.getLocalizationButton(update, BACK), RAA_QUIT_FROM_CHOOSER_CHECK_IN));
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
            buttons.add(msgBuilder.buildIKButton("◀️", RAA_PREVIOUS_CHECK_IN_YEAR));
        else
            buttons.add(msgBuilder.buildIKButton("🛑", EMPTY));

        buttons.add(msgBuilder.buildIKButton(dateS.getSelectedYear(calendar), RAA_CHANGE_CHECK_IN_YEAR));
        buttons.add(msgBuilder.buildIKButton("▶️", RAA_NEXT_CHECK_IN_YEAR));
        keyboard.add(msgBuilder.buildIKRow(buttons));
        buttons.clear();

        if (today.get(Calendar.MONTH) < calendar.get(Calendar.MONTH) || today.get(Calendar.YEAR) < calendar.get(Calendar.YEAR))
            buttons.add(msgBuilder.buildIKButton("◀️", RAA_PREVIOUS_CHECK_IN_MONTH));
        else
            buttons.add(msgBuilder.buildIKButton("🛑", EMPTY));

        buttons.add(msgBuilder.buildIKButton(
                userLoc.getLocalizationButton(update, "month_" + (calendar.get(Calendar.MONTH) + 1)),
                RAA_CHANGE_CHECK_IN_MONTH));
        buttons.add(msgBuilder.buildIKButton("▶️", RAA_NEXT_CHECK_IN_MONTH));
        keyboard.add(msgBuilder.buildIKRow(buttons));
        buttons.clear();
    }
}
