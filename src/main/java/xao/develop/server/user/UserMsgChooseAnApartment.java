package xao.develop.server.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import xao.develop.model.Amenity;
import xao.develop.model.Apartment;
import xao.develop.model.TempBookingData;
import xao.develop.repository.Persistence;
import xao.develop.server.Server;

import java.util.*;

@Slf4j
@Service
public class UserMsgChooseAnApartment extends UserMsg {

    @Autowired
    Server server;

    @Autowired
    Persistence persistence;

    public void setIsBooking(Update update, boolean isBooking) {
        log.debug("Method setIsBooking, update apartment №{}, set isBooking: {}",
                persistence.selectTempApartmentSelector(server.getChatId(update)).getNumberOfApartment(), isBooking);

        persistence.updateIsBookingApartment(
                persistence.selectTempApartmentSelector(server.getChatId(update)).getNumberOfApartment(),
                isBooking,
                server.getChatId(update));
    }

    public boolean getIsBooking(Update update) {
        return persistence.selectApartment(
                persistence.selectTempApartmentSelector(
                        server.getChatId(update)).getNumberOfApartment()).getIsBooking();
    }

    public Long getUserId(Update update) {
        try {
            return persistence.selectApartment(
                    persistence.selectTempApartmentSelector(
                            server.getChatId(update)).getNumberOfApartment()).getUserId();
        } catch (NullPointerException ex) {
            return 0L;
        }
    }

    public int getSelectedApartment(Update update) {
        return persistence.selectTempApartmentSelector(server.getChatId(update)).getNumberOfApartment();
    };

    public Integer getCurrentApartment(Update update) {
        int selector = persistence.selectTempApartmentSelector(server.getChatId(update)).getSelector();
        List<Apartment> apartments = persistence.selectAllApartments();

        log.debug("Size of the list of apartment is {}", apartments.size());
        log.debug("Current selector is {}", selector);
        log.debug("Current number of apartment is {}", apartments.get(selector).getNumber());

        return apartments.get(selector).getNumber();
    }

    public void addTempApartmentSelector(Update update) {
        log.trace("Method addTempApartmentSelector(Update) started");
        persistence.insertTempApartmentSelector(server.getChatId(update));

        log.debug("""
                Method addTempApartmentSelector(Update): added the next user with parameters:
                chatId = {}
                current number of apartment = {}""",
                server.getChatId(update), persistence.selectTempApartmentSelector(server.getChatId(update)));
        log.trace("Method addTempApartmentSelector(Update) finished");
    }

    public void upSelector(Update update) {
        int selector = persistence.selectTempApartmentSelector(server.getChatId(update)).getSelector() + 1;
        if (selector < persistence.selectAllApartments().size()) {
            persistence.updateTempApartmentSelector(
                    server.getChatId(update),
                    persistence.selectAllApartments().get(selector).getNumber(),
                    selector);

            log.debug("""
                    Method upSelector(Update): user selector upped:
                    chatId = {}
                    new selector = {}""",
                    server.getChatId(update), selector);
        } else
            log.debug("Method upSelector(Update): can't up selector because {} is max!", selector - 1);
    }

    public void downSelector(Update update) {
        int selector = persistence.selectTempApartmentSelector(server.getChatId(update)).getSelector() - 1;

        if (selector >= 0) {
            persistence.updateTempApartmentSelector(
                    server.getChatId(update),
                    persistence.selectAllApartments().get(selector).getNumber(),
                    selector);

            log.debug("""
                    Method downSelector(Update): user selector downed:
                    chatId = {}
                    new selector = {}""",
                    server.getChatId(update), selector);
        } else
            log.debug("Method downSelector(Update): can't down selector because {} is min!", selector - 1);
    }

    public void deleteTempApartmentSelector(Update update) {
        persistence.deleteTempApartmentSelector(server.getChatId(update));

        log.debug("Method deleteTempApartmentSelector(Update): the next user deleted: {} ", server.getChatId(update));
    }

    @Override
    public Message sendMessage(Update update) throws TelegramApiException {
        server.deleteOldMessages(update);

        List<Apartment> apartments = persistence.selectAllApartments();

        return !apartments.isEmpty() ? showApartments(update) : showNoFreeApartments(update);
    }

    private Message showApartments(Update update) throws TelegramApiException {
        Apartment apartment = persistence.selectApartment(
                persistence.selectTempApartmentSelector(server.getChatId(update)).getNumberOfApartment()
        );

        TempBookingData tempBookingData = persistence.selectTempBookingData(server.getChatId(update));

        String checkIn = getCheckDate(tempBookingData.getCheckIn());
        String checkOut = getCheckDate(tempBookingData.getCheckOut());
        StringBuilder amenities = getAmenities(update, apartment);

        return botConfig.getTelegramClient().execute(msgBuilder.buildSendMessage(update,
                String.format(userLoc.getLocalizationText(update),
                        checkIn,
                        checkOut,
                        apartment.getArea(),
                        amenities),
                getIKMarkup(update)));
    }

    private Message showNoFreeApartments(Update update) throws TelegramApiException {
        update.getCallbackQuery().setData(NO_FREE_APARTMENTS);
        return botConfig.getTelegramClient().execute(msgBuilder.buildSendMessage(update,
                userLoc.getLocalizationText(update),
                getBackIKMarkup(update)));
    }

    private String getCheckDate(Long checkTimeInMillis) {
        Calendar calendar = persistence.getServerPresentTime();

        calendar.setTimeInMillis(checkTimeInMillis);
        String day = calendar.get(Calendar.DAY_OF_MONTH) < 10 ?
                "0" + calendar.get(Calendar.DAY_OF_MONTH) : String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
        String month = calendar.get(Calendar.MONTH) + 1 < 10 ?
                "0" + (calendar.get(Calendar.MONTH) + 1) : String.valueOf(calendar.get(Calendar.MONTH) + 1);

        return String.format("%s/%s/%s", day, month, calendar.get(Calendar.YEAR));
    }

    private StringBuilder getAmenities(Update update, Apartment apartment) {
        StringBuilder amenities = new StringBuilder();

        if (apartment.getAmenities() != null) {
            String[] amenitiesArray = apartment.getAmenities().split("\\$");
            Arrays.sort(amenitiesArray);

            for (String code : amenitiesArray) {
                Amenity amenity = persistence.selectAmenity(Integer.parseInt(code));

                amenities.append(userLoc.getLocaleMessage(update, amenity.getLink())).append("\n");
            }
        } else
            amenities.append("nothing");

        return amenities;
    }

    @Override
    public InlineKeyboardMarkup getIKMarkup(Update update) {
        List<InlineKeyboardButton> buttons = new ArrayList<>();
        int selector = persistence.selectTempApartmentSelector(server.getChatId(update)).getSelector();

        if (selector > 0)
            buttons.add(msgBuilder.buildIKButton("◀️", RAA_PREVIOUS_APARTMENT));
        else
            buttons.add(msgBuilder.buildIKButton("🛑", EMPTY));

        if (selector < persistence.selectAllApartments().size() - 1)
            buttons.add(msgBuilder.buildIKButton("▶️", RAA_NEXT_APARTMENT));
        else
            buttons.add(msgBuilder.buildIKButton("🛑", EMPTY));

        InlineKeyboardRow row1 = msgBuilder.buildIKRow(buttons);

        buttons.clear();
        buttons.add(msgBuilder.buildIKButton(userLoc.getLocalizationButton(update, RAA_BOOK), RAA_BOOK));
        InlineKeyboardRow row2 = msgBuilder.buildIKRow(buttons);

        buttons.clear();
        buttons.add(msgBuilder.buildIKButton(userLoc.getLocalizationButton(update, BACK), RAA_QUIT_FROM_CHOOSER_AN_APARTMENT));
        InlineKeyboardRow row3 = msgBuilder.buildIKRow(buttons);

        return InlineKeyboardMarkup
                .builder()
                .keyboardRow(row1)
                .keyboardRow(row2)
                .keyboardRow(row3)
                .build();
    }

    public InlineKeyboardMarkup getBackIKMarkup(Update update) {
        return InlineKeyboardMarkup
                .builder()
                .keyboardRow(new InlineKeyboardRow(
                        msgBuilder.buildIKButton(userLoc.getLocalizationButton(update, BACK),
                                RAA_QUIT_FROM_CHOOSER_AN_APARTMENT)))
                .build();
    }
}
