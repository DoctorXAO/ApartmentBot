package xao.develop.service.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import xao.develop.model.TempBookingData;
import xao.develop.repository.Persistence;

import java.time.LocalDate;
import java.time.Period;
import java.util.Calendar;

@Slf4j
@Service
public class UserMsgPreviewCard extends UserMessage {

    @Autowired
    Persistence persistence;

    public int insertCardToBookingCard(Update update) {
        TempBookingData tempBookingData = persistence.selectTempBookingData(service.getChatId(update));

        int numberOfApartment = tempBookingData.getNumberOfApartment();

        int numOfCard = persistence.insertBookingCard(
                tempBookingData.getChatId(),
                service.getUser(update).getUserName(),
                tempBookingData.getFirstName(),
                tempBookingData.getLastName(),
                tempBookingData.getContacts(),
                tempBookingData.getAge(),
                tempBookingData.getGender(),
                tempBookingData.getCountOfPeople(),
                numberOfApartment,
                tempBookingData.getCheckIn(),
                tempBookingData.getCheckOut(),
                getTotalRent(
                        tempBookingData.getCheckIn(),
                        tempBookingData.getCheckOut(),
                        tempBookingData.getCountOfPeople())
        );

        persistence.updateIsBookingApartment(numberOfApartment, false, service.getChatId(update));
        persistence.deleteTempBookingData(service.getChatId(update));
        persistence.deleteTempApartmentSelector(service.getChatId(update));

        return numOfCard;
    }

    public Object[] getPackParameters(Update update) {
        TempBookingData tempBookingData = persistence.selectTempBookingData(service.getChatId(update));

        return new Object[]{
                tempBookingData.getNumberOfApartment(),
                service.getCheckDate(tempBookingData.getCheckIn()),
                service.getCheckDate(tempBookingData.getCheckOut()),
                tempBookingData.getFirstName(),
                tempBookingData.getLastName(),
                tempBookingData.getAge(),
                tempBookingData.getGender(),
                tempBookingData.getCountOfPeople(),
                tempBookingData.getContacts(),
                getTotalRent(
                        tempBookingData.getCheckIn(),
                        tempBookingData.getCheckOut(),
                        tempBookingData.getCountOfPeople())
        };
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
                        msgBuilder.buildIKButton(service.getLocaleMessage(service.getChatId(update), USER_BT_SEND),
                                RAA_SEND_BOOKING_TO_ADMIN)))
                .keyboardRow(new InlineKeyboardRow(
                        msgBuilder.buildIKButton(service.getLocaleMessage(service.getChatId(update), GENERAL_BT_BACK),
                                RAA_QUIT_FROM_PREVIEW_CARD)))
                .build();
    }
}
