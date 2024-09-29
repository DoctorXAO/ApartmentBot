package xao.develop.server.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import xao.develop.model.TempBookingData;
import xao.develop.repository.Persistence;

import java.time.LocalDate;
import java.time.Period;
import java.util.Calendar;

@Slf4j
@Service
public class UserMsgPreviewCard extends UserMsg {

    @Autowired
    Persistence persistence;

    public String getCheckDate(Long checkTimeInMillis) {
        Calendar calendar = persistence.getServerPresentTime();

        calendar.setTimeInMillis(checkTimeInMillis);
        String day = calendar.get(Calendar.DAY_OF_MONTH) < 10 ?
                "0" + calendar.get(Calendar.DAY_OF_MONTH) : String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
        String month = calendar.get(Calendar.MONTH) + 1 < 10 ?
                "0" + (calendar.get(Calendar.MONTH) + 1) : String.valueOf(calendar.get(Calendar.MONTH) + 1);

        return String.format("%s/%s/%s", day, month, calendar.get(Calendar.YEAR));
    }

    public int getTotalRent(long checkIn, long checkOut, int countOfPeople) {

        Calendar calendarCheckIn = persistence.getServerPresentTime();
        calendarCheckIn.setTimeInMillis(checkIn);

        Calendar calendarCheckOut = persistence.getServerPresentTime();
        calendarCheckOut.setTimeInMillis(checkOut);

        LocalDate checkInDate = LocalDate.of(
                calendarCheckIn.get(Calendar.YEAR),
                calendarCheckIn.get(Calendar.MONTH) + 1,
                calendarCheckIn.get(Calendar.DAY_OF_MONTH));

        LocalDate checkOutDate = LocalDate.of(
                calendarCheckOut.get(Calendar.YEAR),
                calendarCheckOut.get(Calendar.MONTH) + 1,
                calendarCheckOut.get(Calendar.DAY_OF_MONTH));

        Period period = Period.between(checkInDate, checkOutDate);

        int years = period.getYears();
        int months = period.getMonths();
        int days = period.getDays();

        log.debug("""
                Period has next parameters:
                years = {}
                months = {}
                days = {}""", years, months, days);

        int costPerYear = botConfig.getPerYear(countOfPeople);
        int costPerMonth = botConfig.getPerMonth(countOfPeople);
        int costPerDay = botConfig.getPerDay(countOfPeople);

        return (years * costPerYear) + (months * costPerMonth) + (days * costPerDay);
    }

    @Override
    public InlineKeyboardMarkup getIKMarkup(Update update) {
        return InlineKeyboardMarkup
                .builder()
                .keyboardRow(new InlineKeyboardRow(
                        msgBuilder.buildIKButton(server.getLocaleMessage(update, USER_BT_SEND),
                                RAA_SEND_BOOKING_TO_ADMIN)))
                .keyboardRow(new InlineKeyboardRow(
                        msgBuilder.buildIKButton(server.getLocaleMessage(update, USER_BT_BACK),
                                RAA_QUIT_FROM_PREVIEW_CARD)))
                .build();
    }
}
