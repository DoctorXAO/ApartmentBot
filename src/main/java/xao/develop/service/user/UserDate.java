package xao.develop.service.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.objects.Update;
import xao.develop.model.TempBookingData;
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

    void setYear(Update update, int year) {
        Calendar selectedTime = getSelectedTime(update);

        selectedTime.set(Calendar.YEAR, year);

        persistence.updateSelectedTimeInTempBookingData(service.getChatId(update),
                Math.max(getPresentTime(update).getTimeInMillis(), selectedTime.getTimeInMillis()));
    }

    void setCheckIn(Update update, int day) {
        log.trace("Method setCheckIn(Update, int) started");

        Calendar selectedTime = getSelectedTime(update);
        selectedTime.set(Calendar.DAY_OF_MONTH, day);
        selectedTime.set(Calendar.HOUR_OF_DAY, botConfig.getCheckInHours());
        persistence.updateCheckInInTempBookingData(service.getChatId(update), selectedTime.getTimeInMillis());

        if (day == getMaxDaysOfMonth(getSelectedTime(update), 0))
            nextMonth(update);

        log.debug("For user {} was set check-in with the next parameters: {}", service.getChatId(update), selectedTime);
        log.trace("Method setCheckIn(Update, int) finished");
    }

    void setCheckOut(Update update, int day) {
        log.trace("Method setCheckOut(Update, int) started");

        Calendar selectedTime = getSelectedTime(update);
        selectedTime.set(Calendar.DAY_OF_MONTH, day);
        selectedTime.set(Calendar.HOUR_OF_DAY, botConfig.getCheckOutHours());
        persistence.updateCheckOutInTempBookingData(service.getChatId(update), selectedTime.getTimeInMillis());

        log.debug("For user {} was set check-out with the next parameters: {}", service.getChatId(update), selectedTime);
        log.trace("Method setCheckOut(Update, int) finished");
    }

    Calendar getPresentTime(Update update) {
        if (!isCheckInSet(update))
            return persistence.getServerPresentTime();
        else {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(persistence.selectTempBookingData(service.getChatId(update)).getCheckIn());
            calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + 1);
            return calendar;
        }
    }

    Calendar getSelectedTime(Update update) {
        Calendar selectedTime = Calendar.getInstance();

        selectedTime.setTimeInMillis(persistence.selectTempBookingData(service.getChatId(update)).getSelectedTime());

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

    boolean isCheckInSet(Update update) {
        log.debug("isCheckInSet for user {}: {}",
                service.getChatId(update), persistence.selectTempBookingData(service.getChatId(update)).getCheckIn() != null);

        return persistence.selectTempBookingData(service.getChatId(update)).getCheckIn() != null;
    }

    boolean isCheckOutSet(Update update) {
        long checkOut = persistence.selectTempBookingData(service.getChatId(update)).getCheckOut();

        log.debug("isCheckIOutSet: checkOut = {}", checkOut);

        return persistence.selectTempBookingData(service.getChatId(update)).getCheckOut() != null;
    }

    void deleteCheckIn(Update update) {
        log.trace("Method deleteCheckIn(Update) started");

        persistence.updateSelectedTimeInTempBookingData(service.getChatId(update),
                persistence.selectTempBookingData(service.getChatId(update)).getCheckIn());
        persistence.deleteCheckInInTempBookingData(service.getChatId(update));

        log.trace("Method deleteCheckIn(Update) finished");
    }

    void deleteCheckOut(Update update) {
        log.trace("Method deleteCheckOut(Update) started");

        persistence.updateSelectedTimeInTempBookingData(service.getChatId(update),
                persistence.selectTempBookingData(service.getChatId(update)).getCheckOut());
        persistence.deleteCheckOutInTempBookingData(service.getChatId(update));

        log.trace("Method deleteCheckOut(Update) finished");
    }

    void addNewUserToTempBookingData(Update update) {
        persistence.insertTempBookingData(
                service.getChatId(update),
                persistence.getServerPresentTime().getTimeInMillis(),
                update.getCallbackQuery().getFrom().getUserName());
    }

    void nextMonth(Update update) {
        Calendar selectedTime = getSelectedTime(update);

        selectedTime.set(Calendar.MONTH, selectedTime.get(Calendar.MONTH) + 1);

        persistence.updateSelectedTimeInTempBookingData(service.getChatId(update), selectedTime.getTimeInMillis());
    }

    void previousMonth(Update update) {
        Calendar selectedTime = getSelectedTime(update);

        selectedTime.set(Calendar.MONTH, selectedTime.get(Calendar.MONTH) - 1);

        persistence.updateSelectedTimeInTempBookingData(service.getChatId(update), selectedTime.getTimeInMillis());
    }

    void nextYear(Update update) {
        Calendar selectedTime = getSelectedTime(update);

        log.debug("Selected time before: {}", selectedTime);

        selectedTime.set(Calendar.YEAR, selectedTime.get(Calendar.YEAR) + 1);

        log.debug("Selected time after: {}", selectedTime);

        Calendar maxCalendar = getPresentTime(update);
        maxCalendar.set(Calendar.YEAR, maxCalendar.get(Calendar.YEAR) + MAX_YEAR);

        persistence.updateSelectedTimeInTempBookingData(service.getChatId(update),
                Math.min(maxCalendar.getTimeInMillis(), selectedTime.getTimeInMillis()));
    }

    void previousYear(Update update) {
        Calendar selectedTime = getSelectedTime(update);

        selectedTime.set(Calendar.YEAR, selectedTime.get(Calendar.YEAR) - 1);

        persistence.updateSelectedTimeInTempBookingData(service.getChatId(update),
                Math.max(getPresentTime(update).getTimeInMillis(), selectedTime.getTimeInMillis()));
    }

    boolean checkEqualsDate(Update update) {
        Calendar presentTime = getPresentTime(update);
        Calendar selectedTime = getSelectedTime(update);

        boolean areMonthsEqual = presentTime.get(Calendar.MONTH) == selectedTime.get(Calendar.MONTH);
        boolean areYearsEqual = presentTime.get(Calendar.YEAR) == selectedTime.get(Calendar.YEAR);

        return areMonthsEqual && areYearsEqual;
    }

    void setSelectedMonth(Update update, int month) {
        log.debug("Selected number of month: {}", month);

        if (month >= 0 && month <= 11) {
            Calendar selectedTime = getSelectedTime(update);
            selectedTime.set(Calendar.MONTH, month);
            persistence.updateSelectedTimeInTempBookingData(service.getChatId(update), selectedTime.getTimeInMillis());
        } else
            log.warn("Unknown number of month: {}", month);
    }
}
