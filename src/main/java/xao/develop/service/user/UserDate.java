package xao.develop.service.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import xao.develop.repository.Persistence;
import xao.develop.service.BotService;

import java.util.Calendar;

@Slf4j
public abstract class UserDate extends UserMessage {

    @Autowired
    Persistence persistence;

    @Autowired
    BotService service;

    final int MAX_YEAR = 1;

    void setYear(long chatId, int year) {
        Calendar selectedTime = getSelectedTime(chatId);

        selectedTime.set(Calendar.YEAR, year);

        persistence.updateSelectedTimeInTempBookingData(chatId,
                Math.max(getPresentTime(chatId).getTimeInMillis(), selectedTime.getTimeInMillis()));
    }

    void setCheckIn(long chatId, int day) {
        log.trace("Method setCheckIn(Update, int) started");

        Calendar selectedTime = getSelectedTime(chatId);
        selectedTime.set(Calendar.DAY_OF_MONTH, day);
        selectedTime.set(Calendar.HOUR_OF_DAY, botConfig.getCheckInHours());
        persistence.updateCheckInInTempBookingData(chatId, selectedTime.getTimeInMillis());

        if (day == getMaxDaysOfMonth(getSelectedTime(chatId), 0))
            nextMonth(chatId);

        log.debug("For user {} was set check-in with the next parameters: {}", chatId, selectedTime);
        log.trace("Method setCheckIn(Update, int) finished");
    }

    void setCheckOut(long chatId, int day) {
        log.trace("Method setCheckOut(Update, int) started");

        Calendar selectedTime = getSelectedTime(chatId);
        selectedTime.set(Calendar.DAY_OF_MONTH, day);
        selectedTime.set(Calendar.HOUR_OF_DAY, botConfig.getCheckOutHours());
        persistence.updateCheckOutInTempBookingData(chatId, selectedTime.getTimeInMillis());

        log.debug("For user {} was set check-out with the next parameters: {}", chatId, selectedTime);
        log.trace("Method setCheckOut(Update, int) finished");
    }

    Calendar getPresentTime(long chatId) {
        if (!isCheckInSet(chatId))
            return persistence.getServerPresentTime();
        else {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(persistence.selectTempBookingData(chatId).getCheckIn());
            calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + 1);
            return calendar;
        }
    }

    Calendar getSelectedTime(long chatId) {
        Calendar selectedTime = Calendar.getInstance();

        selectedTime.setTimeInMillis(persistence.selectTempBookingData(chatId).getSelectedTime());

        return selectedTime;
    }

    String getSelectedYear(Calendar calendar) {
        return String.valueOf(calendar.get(Calendar.YEAR));
    }

    Integer getMaxDaysOfMonth(Calendar calendar, int bias) {
        Calendar c = (Calendar) calendar.clone();

        c.set(Calendar.MONTH, c.get(Calendar.MONTH) + bias);

        return c.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    Integer getFirstDayOfWeekInMonth(Calendar calendar) {
        Calendar c = (Calendar) calendar.clone();

        c.set(Calendar.DAY_OF_MONTH, 1);

        return c.get(Calendar.DAY_OF_WEEK) - 1 == 0 ? 7 : c.get(Calendar.DAY_OF_WEEK) - 1;
    }

    Integer getDayOfWeekInMonth(Calendar calendar, int day) {
        Calendar c = (Calendar) calendar.clone();

        c.set(Calendar.DAY_OF_MONTH, day);

        return c.get(Calendar.DAY_OF_WEEK) - 1 == 0 ? 7 : c.get(Calendar.DAY_OF_WEEK) - 1;
    }

    Integer getPresentDayOfMonth(Calendar calendar) {
        return calendar.get(Calendar.DAY_OF_MONTH);
    }

    boolean isCheckInSet(long chatId) {
        return persistence.selectTempBookingData(chatId).getCheckIn() != null;
    }

    boolean isCheckOutSet(long chatId) {
        return persistence.selectTempBookingData(chatId).getCheckOut() != null;
    }

    void deleteCheckIn(long chatId) {
        log.trace("Method deleteCheckIn(Update) started");

        persistence.updateSelectedTimeInTempBookingData(chatId, persistence.selectTempBookingData(chatId).getCheckIn());
        persistence.deleteCheckInInTempBookingData(chatId);

        log.trace("Method deleteCheckIn(Update) finished");
    }

    void deleteCheckOut(Update update) {
        log.trace("Method deleteCheckOut(Update) started");

        persistence.updateSelectedTimeInTempBookingData(service.getChatId(update),
                persistence.selectTempBookingData(service.getChatId(update)).getCheckOut());
        persistence.deleteCheckOutInTempBookingData(service.getChatId(update));

        log.trace("Method deleteCheckOut(Update) finished");
    }

    void addNewUserToTempBookingData(long chatId, User user) {
        persistence.insertTempBookingData(
                chatId,
                persistence.getServerPresentTime().getTimeInMillis(),
                user.getUserName());
    }

    void nextMonth(long chatId) {
        Calendar selectedTime = getSelectedTime(chatId);

        selectedTime.set(Calendar.MONTH, selectedTime.get(Calendar.MONTH) + 1);

        persistence.updateSelectedTimeInTempBookingData(chatId, selectedTime.getTimeInMillis());
    }

    void previousMonth(long chatId) {
        Calendar selectedTime = getSelectedTime(chatId);

        selectedTime.set(Calendar.MONTH, selectedTime.get(Calendar.MONTH) - 1);

        persistence.updateSelectedTimeInTempBookingData(chatId, selectedTime.getTimeInMillis());
    }

    void nextYear(long chatId) {
        Calendar selectedTime = getSelectedTime(chatId);

        log.debug("Selected time before: {}", selectedTime);

        selectedTime.set(Calendar.YEAR, selectedTime.get(Calendar.YEAR) + 1);

        log.debug("Selected time after: {}", selectedTime);

        Calendar maxCalendar = getPresentTime(chatId);
        maxCalendar.set(Calendar.YEAR, maxCalendar.get(Calendar.YEAR) + MAX_YEAR);

        persistence.updateSelectedTimeInTempBookingData(chatId,
                Math.min(maxCalendar.getTimeInMillis(), selectedTime.getTimeInMillis()));
    }

    void previousYear(Update update) {
        Calendar selectedTime = getSelectedTime(service.getChatId(update));

        selectedTime.set(Calendar.YEAR, selectedTime.get(Calendar.YEAR) - 1);

        persistence.updateSelectedTimeInTempBookingData(service.getChatId(update),
                Math.max(getPresentTime(service.getChatId(update)).getTimeInMillis(), selectedTime.getTimeInMillis()));
    }

    boolean checkEqualsDate(long chatId) {
        Calendar presentTime = getPresentTime(chatId);
        Calendar selectedTime = getSelectedTime(chatId);

        boolean areMonthsEqual = presentTime.get(Calendar.MONTH) == selectedTime.get(Calendar.MONTH);
        boolean areYearsEqual = presentTime.get(Calendar.YEAR) == selectedTime.get(Calendar.YEAR);

        return areMonthsEqual && areYearsEqual;
    }

    void setSelectedMonth(Update update, int month) {
        log.debug("Selected number of month: {}", month);

        if (month >= 0 && month <= 11) {
            Calendar selectedTime = getSelectedTime(service.getChatId(update));
            selectedTime.set(Calendar.MONTH, month);
            persistence.updateSelectedTimeInTempBookingData(service.getChatId(update), selectedTime.getTimeInMillis());
        } else
            log.warn("Unknown number of month: {}", month);
    }
}
