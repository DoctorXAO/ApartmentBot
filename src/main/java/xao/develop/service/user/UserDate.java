package xao.develop.service.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.objects.User;
import xao.develop.config.enums.CheckDate;
import xao.develop.config.enums.Selector;
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

    void setCheck(long chatId, int day, CheckDate type) {
        log.trace("Method setCheckIn(Update, int) started");

        Calendar selectedTime = getSelectedTime(chatId);
        selectedTime.set(Calendar.DAY_OF_MONTH, day);

        switch (type) {
            case IN -> {
                selectedTime.set(Calendar.HOUR_OF_DAY, botConfig.getCheckInHours());
                persistence.updateCheckInInTempBookingData(chatId, selectedTime.getTimeInMillis());

                if (day == getMaxDaysOfMonth(getSelectedTime(chatId)))
                    changeDate(chatId, CheckDate.MONTH, Selector.NEXT);

                log.debug("For user {} was set check-in with the next parameters: {}", chatId, selectedTime);
            }
            case OUT -> {
                selectedTime.set(Calendar.HOUR_OF_DAY, botConfig.getCheckOutHours());
                persistence.updateCheckOutInTempBookingData(chatId, selectedTime.getTimeInMillis());

                log.debug("For user {} was set check-out with the next parameters: {}", chatId, selectedTime);
            }
        }

        log.trace("Method setCheckIn(Update, int) finished");
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

    Integer getMaxDaysOfMonth(Calendar calendar) {
        Calendar c = (Calendar) calendar.clone();

        c.set(Calendar.MONTH, c.get(Calendar.MONTH));

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

    void deleteCheckIn(long chatId) {
        log.trace("Method deleteCheckIn(Update) started");

        persistence.updateSelectedTimeInTempBookingData(chatId, persistence.selectTempBookingData(chatId).getCheckIn());
        persistence.deleteCheckInInTempBookingData(chatId);

        log.trace("Method deleteCheckIn(Update) finished");
    }

    void deleteCheckOut(long chatId) {
        log.trace("Method deleteCheckOut(Update) started");
        
        persistence.updateSelectedTimeInTempBookingData(chatId,
                persistence.selectTempBookingData(chatId).getCheckOut());
        persistence.deleteCheckOutInTempBookingData(chatId);

        log.trace("Method deleteCheckOut(Update) finished");
    }

    void addNewUserToTempBookingData(long chatId, User user) {
        persistence.insertTempBookingData(
                chatId,
                persistence.getServerPresentTime().getTimeInMillis(),
                user.getUserName());
    }

    void changeDate(long chatId, CheckDate date, Selector type) {
        Calendar selectedTime = getSelectedTime(chatId);

        if (date.equals(CheckDate.YEAR))
            switch (type) {
                case NEXT -> nextYear(chatId, selectedTime);
                case PREVIOUS -> previousYear(chatId, selectedTime);
            }
        else if (date.equals(CheckDate.MONTH)) {
            switch (type) {
                case NEXT -> nextMonth(chatId, selectedTime);
                case PREVIOUS -> previousMonth(chatId, selectedTime);
            }
        }
    }

    boolean checkEqualsDate(long chatId) {
        Calendar presentTime = getPresentTime(chatId);
        Calendar selectedTime = getSelectedTime(chatId);

        boolean areMonthsEqual = presentTime.get(Calendar.MONTH) == selectedTime.get(Calendar.MONTH);
        boolean areYearsEqual = presentTime.get(Calendar.YEAR) == selectedTime.get(Calendar.YEAR);

        return areMonthsEqual && areYearsEqual;
    }

    void setSelectedMonth(long chatId, int month) {
        log.debug("Selected number of month: {}", month);

        if (month >= 0 && month <= 11) {
            Calendar selectedTime = getSelectedTime(chatId);
            selectedTime.set(Calendar.MONTH, month);
            persistence.updateSelectedTimeInTempBookingData(chatId, selectedTime.getTimeInMillis());
        } else
            log.warn("Unknown number of month: {}", month);
    }


    private void nextYear(long chatId, Calendar selectedTime) {
        selectedTime.set(Calendar.YEAR, selectedTime.get(Calendar.YEAR) + 1);

        Calendar maxCalendar = getPresentTime(chatId);
        maxCalendar.set(Calendar.YEAR, maxCalendar.get(Calendar.YEAR) + MAX_YEAR);

        persistence.updateSelectedTimeInTempBookingData(chatId,
                Math.min(maxCalendar.getTimeInMillis(), selectedTime.getTimeInMillis()));
    }

    private void previousYear(long chatId, Calendar selectedTime) {
        selectedTime.set(Calendar.YEAR, selectedTime.get(Calendar.YEAR) - 1);

        persistence.updateSelectedTimeInTempBookingData(chatId,
                Math.max(getPresentTime(chatId).getTimeInMillis(), selectedTime.getTimeInMillis()));
    }

    private void nextMonth(long chatId, Calendar selectedTime) {
        selectedTime.set(Calendar.MONTH, selectedTime.get(Calendar.MONTH) + 1);

        persistence.updateSelectedTimeInTempBookingData(chatId, selectedTime.getTimeInMillis());
    }

    private void previousMonth(long chatId, Calendar selectedTime) {
        selectedTime.set(Calendar.MONTH, selectedTime.get(Calendar.MONTH) - 1);

        persistence.updateSelectedTimeInTempBookingData(chatId, selectedTime.getTimeInMillis());
    }
}
