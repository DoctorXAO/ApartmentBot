package xao.develop.service.user;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.User;
import xao.develop.command.UserCommand;
import xao.develop.command.UserMessageLink;
import xao.develop.enums.Selector;
import xao.develop.enums.AppStatus;
import xao.develop.model.*;
import xao.develop.service.BotMessage;

import java.time.LocalDate;
import java.time.Period;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;

@Slf4j
public abstract class UserMessage extends BotMessage implements UserCommand, UserMessageLink {

    // setters

    public void setIsBookingApartment(long chatId, boolean isBooking) {
        int numberOfApartment = getSelectedApartment(chatId);

        log.debug("Method setIsBooking, update apartment №{}, set isBooking: {}", numberOfApartment, isBooking);

        persistence.updateIsBookingApartment(numberOfApartment, isBooking, chatId);
        persistence.updateNumberOfApartmentTempBookingData(chatId, numberOfApartment);
    }

    // getters

    public int getSelectorOfCurrentApartment(long chatId) {
        TempApartmentSelector tempApartmentSelector = persistence.selectTempApartmentSelector(chatId);

        if (tempApartmentSelector == null)
            return 0;
        else
            return tempApartmentSelector.getSelector();
    }

    public int getCurrentApartment(long chatId) {
        int selector = getSelectorOfCurrentApartment(chatId);
        List<Apartment> apartments = persistence.selectAllFreeApartments(chatId);

        Integer numberOfApartment = apartments.get(selector).getNumber();

        log.debug("Size of the list of apartment is {}", apartments.size());
        log.debug("Current selector is {}", selector);
        log.debug("Current number of apartment is {}", numberOfApartment);

        // todo new проверил, до вызова функции стоит проверка на пустой лист. Отработать функцию и убедиться, что нет ошибок
        // todo Проверить функционал при отсутствии квартир, раньше выбивало ошибку и стоял чек на section = 0. Но он равен 0 по умолчанию, по этому либо менять секцион по умолчанию, либо чет делать

        return numberOfApartment;
    }

    public int getCountOfFreeApartments(long chatId) {
        return persistence.selectAllFreeApartments(chatId).size();
    }

    public long getBookingUserIdApartment(long chatId) {
        try {
            return persistence.selectApartment(getSelectedApartment(chatId)).getUserId();
        } catch (NullPointerException ex) {
            return 0L;
        }
    }

    public Object[] getApartmentParameters(long chatId) {
        Apartment apartment = persistence.selectApartment(getSelectedApartment(chatId));

        StringBuilder amenities = getAmenities(chatId, apartment);

        int currentSelectorOfApartment = getSelectorOfCurrentApartment(chatId) + 1;
        int maxFreeApartments = getCountOfFreeApartments(chatId);

        return new Object[]{apartment.getArea(), amenities, currentSelectorOfApartment, maxFreeApartments};
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

    public Object[] getPackParameters(long chatId) {
        TempBookingData tempBookingData = persistence.selectTempBookingData(chatId);

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

    private int getSelectedApartment(long chatId) {
        TempApartmentSelector tempApartmentSelector = persistence.selectTempApartmentSelector(chatId);

        if (tempApartmentSelector == null)
            return 0;
        else
            return tempApartmentSelector.getNumberOfApartment();
    }

    private StringBuilder getAmenities(long chatId, Apartment apartment) {
        StringBuilder amenities = new StringBuilder();

        List<Amenity> listOfAmenities = apartment.getAmenities();
        listOfAmenities.sort(Comparator.comparingInt(Amenity::getImportance));

        if (listOfAmenities.isEmpty())
            amenities.append("null");
        else
            for (Amenity amenity : listOfAmenities)
                amenities.append(service.getLocaleMessage(chatId, amenity.getLink())).append("\n");

        return amenities;
    }

    // boolean

    public boolean isAlreadyExistRent(long chatId) {
        List<BookingCard> bookingCards = persistence.selectBookingCardByStatus(AppStatus.WAITING);
        bookingCards.addAll(persistence.selectBookingCardByStatus(AppStatus.ACCEPTED));

        for (BookingCard bookingCard : bookingCards)
            if (bookingCard.getChatId() == chatId)
                return true;

        return false;
    }

    public boolean isBookingApartment(long chatId) {
        return persistence.selectApartment(getSelectedApartment(chatId)).getIsBooking();
    }

    public boolean isApartmentsEmpty(long chatId) {
        return persistence.selectAllFreeApartments(chatId).isEmpty();
    }

    // actions

    public void addTempApartmentSelector(long chatId) {
        log.trace("Method addTempApartmentSelector(Update) started");

        persistence.insertTempApartmentSelector(chatId);

        log.trace("Method addTempApartmentSelector(Update) finished");
    }

    public void changeSelector(long chatId, Selector type) {
        int selector = getSelectorOfCurrentApartment(chatId);

        switch (type) {
            case NEXT -> {
                selector += 1;

                if (selector < getCountOfFreeApartments(chatId))
                    updateSelector(chatId, selector);
                else
                    log.debug("Method changeSelector(Update): can't up selector because {} is max!", selector);
            }
            case PREVIOUS -> {
                selector -= 1;

                if (selector >= 0)
                    updateSelector(chatId, selector);
                else
                    log.debug("Method changeSelector(Update): can't up selector because {} is min!", selector);
            }
        }
    }

    public void insertCardToBookingCard(long chatId, User user) {
        TempBookingData tempBookingData = persistence.selectTempBookingData(chatId);

        int numberOfApartment = tempBookingData.getNumberOfApartment();

        persistence.insertBookingCard(
                tempBookingData.getChatId(),
                user.getUserName(),
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

        persistence.updateIsBookingApartment(numberOfApartment, false, chatId);
        persistence.deleteTempBookingData(chatId);
        persistence.deleteTempApartmentSelector(chatId);
    }

    private void updateSelector(long chatId, int selector) {
        persistence.updateTempApartmentSelector(
                chatId,
                persistence.selectAllFreeApartments(chatId).get(selector).getNumber(),
                selector);

        log.debug("""
                    Method update(Update): user selector updated:
                    chatId = {}
                    new selector = {}""", chatId, selector);
    }

    public void deleteTempApartmentSelector(long chatId) {
        persistence.deleteTempApartmentSelector(chatId);

        log.debug("Method deleteTempApartmentSelector(Update): the next user deleted: {} ", chatId);
    }

    public void deleteUserFromTempBookingData(long chatId) {
        persistence.deleteTempBookingData(chatId);

        log.debug("The next user from UserCalendar deleted: {}", chatId);
    }

    // markups
}
