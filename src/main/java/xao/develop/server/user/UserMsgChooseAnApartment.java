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
import xao.develop.repository.Persistence;

import java.util.*;

@Slf4j
@Service
public class UserMsgChooseAnApartment extends UserMsg {

    @Autowired
    Persistence persistence;

    public void setIsBooking(Update update, boolean isBooking) {
        int numberOfApartment = getSelectedApartment(update);

        log.debug("Method setIsBooking, update apartment №{}, set isBooking: {}", numberOfApartment, isBooking);

        persistence.updateIsBookingApartment(numberOfApartment, isBooking, server.getChatId(update));
        persistence.updateNumberOfApartmentTempBookingData(server.getChatId(update), numberOfApartment);
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

    private int getSelectedApartment(Update update) {
        return persistence.selectTempApartmentSelector(server.getChatId(update)).getNumberOfApartment();
    }

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

    public Message sendMessage(Update update) throws TelegramApiException {
        server.deleteOldMessages(update);

        List<Apartment> apartments = persistence.selectAllApartments();

        return !apartments.isEmpty() ? showApartments(update) : showNoFreeApartments(update);
    }

    private Message showApartments(Update update) throws TelegramApiException {
        Apartment apartment = persistence.selectApartment(
                persistence.selectTempApartmentSelector(server.getChatId(update)).getNumberOfApartment()
        );

        StringBuilder amenities = getAmenities(update, apartment);

        return botConfig.getTelegramClient().execute(server.sendMessage(update,
                String.format(server.getLocaleMessage(update, USER_MSG_CHOOSE_AN_APARTMENT),
                        apartment.getArea(),
                        amenities),
                getIKMarkup(update)));
    }

    private Message showNoFreeApartments(Update update) throws TelegramApiException {
        update.getCallbackQuery().setData(NO_FREE_APARTMENTS);
        return botConfig.getTelegramClient().execute(server.sendMessage(update,
                server.getLocaleMessage(update, USER_MSG_NO_FREE_APARTMENTS),
                getBackIKMarkup(update)));
    }

    private StringBuilder getAmenities(Update update, Apartment apartment) {
        StringBuilder amenities = new StringBuilder();

        if (apartment.getAmenities() != null) {
            String[] amenitiesArray = apartment.getAmenities().split("\\$");
            Arrays.sort(amenitiesArray);

            for (String code : amenitiesArray) {
                Amenity amenity = persistence.selectAmenity(Integer.parseInt(code));

                amenities.append(server.getLocaleMessage(update, amenity.getLink())).append("\n");
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
        buttons.add(msgBuilder.buildIKButton(server.getLocaleMessage(update, USER_BT_BOOK), RAA_BOOK));
        InlineKeyboardRow row2 = msgBuilder.buildIKRow(buttons);

        buttons.clear();
        buttons.add(msgBuilder.buildIKButton(server.getLocaleMessage(update, USER_BT_BACK), RAA_QUIT_FROM_CHOOSER_AN_APARTMENT));
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
                        msgBuilder.buildIKButton(server.getLocaleMessage(update, USER_BT_BACK),
                                RAA_QUIT_FROM_CHOOSER_AN_APARTMENT)))
                .build();
    }
}
