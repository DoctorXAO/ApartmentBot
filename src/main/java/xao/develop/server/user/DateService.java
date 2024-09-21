package xao.develop.server.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import xao.develop.repository.Persistence;
import xao.develop.server.Server;

import java.util.Calendar;

@Slf4j
@Service
public class DateService {

    @Autowired
    Persistence persistence;

    @Autowired
    Server server;

    Calendar getPresentTime() {
        return persistence.getServerPresentTime();
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

    Integer getCurrentDayOfMonth(Calendar calendar) {
        return calendar.get(Calendar.DAY_OF_MONTH);
    }
}
