package xao.develop.server.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import xao.develop.server.Server;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Service
public class UserMsgChooseCheckInDate extends UserMsg {

    @Autowired
    Server server;

    HashMap<Long, Calendar[]> userCalendar = new HashMap<>();

    public void addUserCalendar(Update update) {
        Calendar[] calendars = new Calendar[2];

        calendars[0] = Calendar.getInstance();
        calendars[1] = (Calendar) calendars[0].clone();

        userCalendar.put(server.getChatId(update), calendars);

        log.debug("Added new user to UserCalendar: {}", server.getChatId(update));
    }

    public void deleteUserCalendar(Update update) {
        userCalendar.remove(server.getChatId(update));

        log.debug("The next user from UserCalendar deleted: {}", server.getChatId(update));
    }

    public void nextMonth(Update update) {
        Calendar calendar = userCalendar.get(server.getChatId(update))[1];

        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) + 1);
    }

    public void previousMonth(Update update) {
        Calendar calendar = userCalendar.get(server.getChatId(update))[1];

        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - 1);
    }

    public void nextYear(Update update) {
        Calendar calendar = userCalendar.get(server.getChatId(update))[1];

        calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + 1);
    }

    public void previousYear(Update update) {
        Calendar[] calendars = userCalendar.get(server.getChatId(update));

        calendars[1].set(Calendar.YEAR, calendars[1].get(Calendar.YEAR) - 1);
        if (calendars[1].before(calendars[0]))
            calendars[1] = (Calendar) calendars[0].clone();
    }

    private Calendar getCurrentUserCalendar(Update update) {
        return userCalendar.get(server.getChatId(update))[0];
    }

    private boolean checkEqualsDate(Update update) {
        Calendar[] calendars = userCalendar.get(server.getChatId(update));

        Calendar currentCalendar = calendars[0];
        Calendar selectedCalendar = calendars[1];

        boolean areMonthsEqual = currentCalendar.get(Calendar.MONTH) == selectedCalendar.get(Calendar.MONTH);
        boolean areYearsEqual = currentCalendar.get(Calendar.YEAR) == selectedCalendar.get(Calendar.YEAR);

        log.debug("""
                areMonthsEquals = {}
                currentCalendarMonth = {}
                selectedCalendarMonth = {}""",
                areMonthsEqual, currentCalendar.get(Calendar.MONTH), selectedCalendar.get(Calendar.MONTH));

        log.debug("""
                areYearsEquals = {}
                currentCalendarYear = {}
                selectedCalendarYear = {}""",
                areMonthsEqual, currentCalendar.get(Calendar.YEAR), selectedCalendar.get(Calendar.YEAR));

        return areMonthsEqual && areYearsEqual;
    }

    private String getSelectedYear(Calendar calendar) {
        return String.valueOf(calendar.get(Calendar.YEAR));
    }

    private String getSelectedMonth(Calendar calendar) {
        int numberOfMonth = calendar.get(Calendar.MONTH) + 1;
        String nameOfMonth;

        switch (numberOfMonth) { // todo Сделать локаль
            case 1 -> nameOfMonth = "Январь";
            case 2 -> nameOfMonth = "Февраль";
            case 3 -> nameOfMonth = "Март";
            case 4 -> nameOfMonth = "Апрель";
            case 5 -> nameOfMonth = "Май";
            case 6 -> nameOfMonth = "Июнь";
            case 7 -> nameOfMonth = "Июль";
            case 8 -> nameOfMonth = "Август";
            case 9 -> nameOfMonth = "Сентябрь";
            case 10 -> nameOfMonth = "Октябрь";
            case 11 -> nameOfMonth = "Ноябрь";
            case 12 -> nameOfMonth = "Декабрь";
            default -> {
                log.warn("Incorrect number of month: {}", numberOfMonth);
                nameOfMonth = "null";
            }
        }

        return nameOfMonth;
    }

    private Integer getMaxDaysOfMonth(Calendar calendar, int bias) {
        Calendar c = (Calendar) calendar.clone();

        c.set(Calendar.MONTH, c.get(Calendar.MONTH) + bias);

        return c.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    private Integer getFirstDayOfWeekInMonth(Calendar calendar) {
        Calendar c = (Calendar) calendar.clone();

        c.set(Calendar.DAY_OF_MONTH, 1);

        return c.get(Calendar.DAY_OF_WEEK) - 1 == 0 ? 7 : c.get(Calendar.DAY_OF_WEEK) - 1;
    }

    private Integer getDayOfWeekInMonth(Calendar calendar, int day) {
        Calendar c = (Calendar) calendar.clone();

        c.set(Calendar.DAY_OF_MONTH, day);

        return c.get(Calendar.DAY_OF_WEEK) - 1 == 0 ? 7 : c.get(Calendar.DAY_OF_WEEK) - 1;
    }

    private Integer getCurrentDayOfMonth(Calendar calendar) {
        return calendar.get(Calendar.DAY_OF_MONTH);
    }

    @Override
    public InlineKeyboardMarkup getIKMarkup(Update update) {
        List<InlineKeyboardRow> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> buttons = new ArrayList<>();

        Calendar currentCalendar = userCalendar.get(server.getChatId(update))[0];
        Calendar calendar = userCalendar.get(server.getChatId(update))[1];

        int maxCurrentDaysOfMonth = getMaxDaysOfMonth(calendar, 0);
        int maxPreviousDaysOfMonth = getMaxDaysOfMonth(calendar,-1);
        int firstDayOfWeekInMonth = getFirstDayOfWeekInMonth(calendar);
        int currentDayOfMonth = getCurrentDayOfMonth(currentCalendar);

        log.debug("""
                Check-in parameters:
                maxCurrentDaysOfMonth = {}
                maxPreviousDaysOfMonth = {}
                firstDayOfWeekInMonth = {}
                currentDayOfMonth = {}""",
                maxCurrentDaysOfMonth, maxPreviousDaysOfMonth, firstDayOfWeekInMonth, currentDayOfMonth);

        initHeaderIKMarkup(keyboard, buttons, currentCalendar, calendar);

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
            if (i == maxCurrentDaysOfMonth && getDayOfWeekInMonth(calendar, i) != 7) {
                int difference = 7 - getDayOfWeekInMonth(calendar, i);
                for (int j = 1; j <= difference; j++)
                    buttons.add(msgBuilder.buildIKButton("🛑", EMPTY));
                keyboard.add(msgBuilder.buildIKRow(buttons));
                buttons.clear();
                break;
            }

            // Если последний день недели, то оборвать линию и начать новую
            if (getDayOfWeekInMonth(calendar, i) == 7) {
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

    // todo Сделать функционал Установка выбранного года и выбранного месяца
    private void initHeaderIKMarkup(List<InlineKeyboardRow> keyboard,
                                    List<InlineKeyboardButton> buttons,
                                    Calendar today,
                                    Calendar calendar) {

        if (today.get(Calendar.YEAR) < calendar.get(Calendar.YEAR))
            buttons.add(msgBuilder.buildIKButton("◀️", RAA_PREVIOUS_CHECK_IN_YEAR));
        else
            buttons.add(msgBuilder.buildIKButton("🛑", EMPTY));

        buttons.add(msgBuilder.buildIKButton(getSelectedYear(calendar), RAA_CHANGE_CHECK_IN_YEAR));
        buttons.add(msgBuilder.buildIKButton("▶️", RAA_NEXT_CHECK_IN_YEAR));
        keyboard.add(msgBuilder.buildIKRow(buttons));
        buttons.clear();

        if (today.get(Calendar.MONTH) < calendar.get(Calendar.MONTH) || today.get(Calendar.YEAR) < calendar.get(Calendar.YEAR))
            buttons.add(msgBuilder.buildIKButton("◀️", RAA_PREVIOUS_CHECK_IN_MONTH));
        else
            buttons.add(msgBuilder.buildIKButton("🛑", EMPTY));

        buttons.add(msgBuilder.buildIKButton(getSelectedMonth(calendar), RAA_CHANGE_CHECK_IN_MONTH));
        buttons.add(msgBuilder.buildIKButton("▶️", RAA_NEXT_CHECK_IN_MONTH));
        keyboard.add(msgBuilder.buildIKRow(buttons));
        buttons.clear();
    }
}
