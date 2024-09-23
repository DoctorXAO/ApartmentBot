package xao.develop.server.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.objects.Update;
import xao.develop.repository.Persistence;
import xao.develop.server.Server;

import java.util.Calendar;

@Slf4j
public abstract class UserDate extends UserMsg {

    @Autowired
    Persistence persistence;

    @Autowired
    Server server;

    final int MAX_YEAR = 10;

    void setYear(Update update, int year) {
        Calendar selectedTime = getSelectedTime(update);

        selectedTime.set(Calendar.YEAR, year);

        persistence.updateSelectedTimeInTempBookingData(server.getChatId(update),
                Math.max(getPresentTime(update).getTimeInMillis(), selectedTime.getTimeInMillis()));
    }

    void setCheckIn(Update update, int day) {
        log.trace("Method setCheckIn(Update, int) started");

        Calendar selectedTime = getSelectedTime(update);
        selectedTime.set(Calendar.DAY_OF_MONTH, day);
        persistence.updateCheckInInTempBookingData(server.getChatId(update), selectedTime.getTimeInMillis());

        log.debug("For user {} was set check-in with the next parameters: {}", server.getChatId(update), selectedTime);
        log.trace("Method setCheckIn(Update, int) finished");
    }

    void setCheckOut(Update update, int day) {
        log.trace("Method setCheckOut(Update, int) started");

        Calendar selectedTime = getSelectedTime(update);
        selectedTime.set(Calendar.DAY_OF_MONTH, day);
        persistence.updateCheckOutInTempBookingData(server.getChatId(update), selectedTime.getTimeInMillis());

        log.debug("For user {} was set check-out with the next parameters: {}", server.getChatId(update), selectedTime);
        log.trace("Method setCheckOut(Update, int) finished");
    }

    Calendar getPresentTime(Update update) {
        if (!isCheckInSet(update))
            return persistence.getServerPresentTime();
        else {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(persistence.selectTempBookingData(server.getChatId(update)).getCheckIn());
            calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + 1);
            return calendar;
        }
    }

    Calendar getSelectedTime(Update update) {
        Calendar selectedTime = Calendar.getInstance();

        selectedTime.setTimeInMillis(persistence.selectTempBookingData(server.getChatId(update)).getSelectedTime());

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
                server.getChatId(update), persistence.selectTempBookingData(server.getChatId(update)).getCheckIn() != null);

        return persistence.selectTempBookingData(server.getChatId(update)).getCheckIn() != null;
    }

    boolean isCheckOutSet(Update update) {
        long checkOut = persistence.selectTempBookingData(server.getChatId(update)).getCheckOut();

        log.debug("isCheckIOutSet: checkOut = {}", checkOut);

        return persistence.selectTempBookingData(server.getChatId(update)).getCheckOut() != null;
    }

    void deleteCheckIn(Update update) {
        log.trace("Method deleteCheckIn(Update) started");

        persistence.deleteCheckInInTempBookingData(server.getChatId(update));

        log.trace("Method deleteCheckIn(Update) finished");
    }

    void deleteCheckOut(Update update) {
        log.trace("Method deleteCheckOut(Update) started");

        persistence.deleteCheckOutInTempBookingData(server.getChatId(update));

        log.trace("Method deleteCheckOut(Update) finished");
    }

    void addNewUserToTempBookingData(Update update) {
        persistence.insertTempBookingData(server.getChatId(update), Calendar.getInstance().getTimeInMillis());
    }

    void nextMonth(Update update) {
        Calendar selectedTime = getSelectedTime(update);

        selectedTime.set(Calendar.MONTH, selectedTime.get(Calendar.MONTH) + 1);

        persistence.updateSelectedTimeInTempBookingData(server.getChatId(update), selectedTime.getTimeInMillis());
    }

    void previousMonth(Update update) {
        Calendar selectedTime = getSelectedTime(update);

        selectedTime.set(Calendar.MONTH, selectedTime.get(Calendar.MONTH) - 1);

        persistence.updateSelectedTimeInTempBookingData(server.getChatId(update), selectedTime.getTimeInMillis());
    }

    void nextYear(Update update) {
        Calendar selectedTime = getSelectedTime(update);

        log.debug("Selected time before: {}", selectedTime);

        selectedTime.set(Calendar.YEAR, selectedTime.get(Calendar.YEAR) + 1);

        log.debug("Selected time after: {}", selectedTime);

        persistence.updateSelectedTimeInTempBookingData(server.getChatId(update), selectedTime.getTimeInMillis());
    }

    void previousYear(Update update) {
        Calendar selectedTime = getSelectedTime(update);

        selectedTime.set(Calendar.YEAR, selectedTime.get(Calendar.YEAR) - 1);

        persistence.updateSelectedTimeInTempBookingData(server.getChatId(update),
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
            persistence.updateSelectedTimeInTempBookingData(server.getChatId(update), selectedTime.getTimeInMillis());
        } else
            log.warn("Unknown number of month: {}", month);
    }
}
