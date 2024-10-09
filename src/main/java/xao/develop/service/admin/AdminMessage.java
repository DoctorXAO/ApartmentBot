package xao.develop.service.admin;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import xao.develop.command.AdminCommand;
import xao.develop.command.AdminMessageLink;
import xao.develop.command.GeneralCommand;
import xao.develop.model.*;
import xao.develop.enums.App;
import xao.develop.enums.AppStatus;
import xao.develop.service.BotMessage;

import java.util.List;

@Slf4j
public abstract class AdminMessage extends BotMessage implements AdminCommand, AdminMessageLink, GeneralCommand {

    // setters

    // getters

    public int getCountOfArchive() {
        return persistence.selectAllBookingCardExceptWaiting().size();
    }

    public int getCountOfNewApps() {
        return persistence.selectBookingCardByStatus(AppStatus.WAITING).size();
    }

    public Object[] getAppParameters(long chatId, int idOfCard) {
        BookingCard bookingCard = persistence.selectBookingCard(idOfCard);

        String status;

        switch (bookingCard.getStatus()) {
            case WAITING -> status = service.getLocaleMessage(chatId, ADMIN_MSG_STATUS_WAITING);
            case ACCEPTED -> status = service.getLocaleMessage(chatId, ADMIN_MSG_STATUS_ACCEPTED);
            case DENIED -> status = service.getLocaleMessage(chatId, ADMIN_MSG_STATUS_DENIED);
            case FINISHED -> status = service.getLocaleMessage(chatId, ADMIN_MSG_STATUS_FINISHED);
            default -> status = "null";
        }

        return new Object[]{
                bookingCard.getId(),
                status,
                bookingCard.getNumberOfApartment(),
                service.getCheckDate(bookingCard.getCheckIn()),
                service.getCheckDate(bookingCard.getCheckOut()),
                bookingCard.getFirstName(),
                bookingCard.getLastName(),
                bookingCard.getAge(),
                bookingCard.getGender(),
                bookingCard.getCountOfPeople(),
                "@" + bookingCard.getLogin(),
                bookingCard.getContacts(),
                bookingCard.getCost()
        };
    }

    public String getStatusOfApp(int idOfCard) {
        return persistence.selectBookingCard(idOfCard).getStatus();
    }

    public long getUserId(int numOfApp) {
        return persistence.selectBookingCard(numOfApp).getChatId();
    }

    public int getSelectedApp(long chatId) {
        return persistence.selectTempAdminSettings(chatId).getSelectedApplication();
    }

    public Object[] getTempNewApartmentParameters(long chatId) {
        TempNewApartment newApartment = persistence.selectTempNewApartment(chatId);

        return new Object[]{
                newApartment.getNumber(),
                newApartment.getCountOfPictures(),
                newApartment.getArea(),
                newApartment.getLinksOfAmenities()};
    }

    public boolean isNewApartment(long chatId) {
        return persistence.selectTempAdminSettings(chatId).isNewApartment();
    }

    public TempNewApartment getTempNewApartment(long chatId) {
        return persistence.selectTempNewApartment(chatId);
    }

    // creates

    public void createAdminSettings(long chatId) {
        persistence.insertTempAdminSettings(chatId);
    }

    public void createNewApartmentField(long chatId) {
        persistence.insertTempNewApartment(chatId);
    }

    public void createApartment(int number, double area, String amenities) {
        persistence.insertApartment(number, area, amenities);
    }

    // updates

    public void updateBookingCardStatus(int idOfCard, AppStatus status) {
        persistence.updateBookingCard(idOfCard, status);
    }

    public void updateSelectedAppAdminSettings(long chatId, int selectedApp) {
        persistence.updateSelectedAppTempAdminSettings(chatId, selectedApp);
    }

    public void updateSelectedPageAdminSettings(long chatId, int selectedPage) {
        persistence.updateSelectedPageTempAdminSettings(chatId, selectedPage);
    }

    public void updateNewApartmentAdminSettings(long chatId, boolean isNewApartment) {
        persistence.updateNewApartmentTempAdminSettings(chatId, isNewApartment);
    }

    public void updateNumberTempNewApartment(long chatId, int number) {
        persistence.updateNumberTempNewApartment(chatId, number);
    }

    public void updateCountOfPicturesTempNewApartment(long chatId, long countOfPictures) {
        persistence.updateCountOfPicturesTempNewApartment(chatId, countOfPictures);
    }

    public void updateAreaTempNewApartment(long chatId, double area) {
        persistence.updateAreaTempNewApartment(chatId, area);
    }

    public void updateLinksOfAmenitiesTempNewApartment(long chatId, String linksOfAmenities) {
        persistence.updateLinksOfAmenitiesTempNewApartment(chatId, linksOfAmenities);
    }

    // deletes

    public void deleteAdminSettings(long chatId) {
        persistence.deleteTempAdminSettings(chatId);
    }

    public void deleteTempNewApartment(long chatId) {
        persistence.deleteTempNewApartment(chatId);
    }

    // init

    protected void initSelectorApps(long chatId,
                                    App type,
                                    List<InlineKeyboardRow> keyboard,
                                    List<InlineKeyboardButton> buttons) {

        List<BookingCard> bookingCards = null;
        String data = EMPTY;

        TempAdminSettings adminSettings = persistence.selectTempAdminSettings(chatId);

        if (type.equals(App.APP)) {
            bookingCards = persistence.selectBookingCardByStatus(AppStatus.WAITING);
            data = APP + X;
            initSelectorPanel(chatId, bookingCards.size(), botConfig.getCountOfAppsOnPage(), adminSettings,
                    PREVIOUS_PAGE_OF_NEW_APPS, NEXT_PAGE_OF_NEW_APPS, keyboard, buttons);
        }
        else if (type.equals(App.ARC)) {
            bookingCards = persistence.selectAllBookingCardExceptWaiting();
            data = ARC + X;
            initSelectorPanel(chatId, bookingCards.size(), botConfig.getCountOfAppsOnPage(), adminSettings,
                    PREVIOUS_PAGE_OF_ARCHIVE, NEXT_PAGE_OF_ARCHIVE, keyboard, buttons);
        }

        if (!bookingCards.isEmpty())
            for (int i = 0; i < botConfig.getCountOfAppsOnPage(); i++) {

                if (adminSettings.getSelectedPage() + i >= bookingCards.size())
                    break;

                BookingCard bookingCard = bookingCards.get(adminSettings.getSelectedPage() + i);

                String statusIcon = getStatusIcon(bookingCard.getStatus());

                buttons.add(msgBuilder.buildIKButton(service.getLocaleMessage(chatId,
                        GENERAL_BT_APP,
                        bookingCard.getId(),
                        statusIcon), data + bookingCard.getId()));
                keyboard.add(msgBuilder.buildIKRow(buttons));
                buttons.clear();
            }
    }

    protected void initSelectorApartments(long chatId,
                                          List<InlineKeyboardRow> keyboard,
                                          List<InlineKeyboardButton> buttons) {
        try {
            List<Apartment> apartments = persistence.selectAllApartmentsSortByNumber();

            for(Apartment apartment : apartments) {
                TempAdminSettings adminSettings = persistence.selectTempAdminSettings(chatId);

                initSelectorPanel(chatId, apartments.size(), botConfig.getCountOfApartmentOnPage(), adminSettings,
                        PREVIOUS_PAGE_OF_APART, NEXT_PAGE_OF_APART, keyboard, buttons);

                for (int i = 0; i < botConfig.getCountOfApartmentOnPage(); i++) {

                    if (adminSettings.getSelectedPage() + i >= apartments.size())
                        break;

                    buttons.add(msgBuilder.buildIKButton(service.getLocaleMessage(chatId,
                            ADMIN_BT_APARTMENT,
                            apartment.getNumber()), APARTMENT + X + apartment.getNumber()));
                    keyboard.add(msgBuilder.buildIKRow(buttons));
                    buttons.clear();
                }
            }
        } catch (Exception ex) {
            log.debug("initSelectorApartments exception: {}", ex.getMessage());
        }
    }

    protected void initSelectorAmenities(long chatId,
                                         List<InlineKeyboardRow> keyboard,
                                         List<InlineKeyboardButton> buttons) {

        buttons.add(msgBuilder.buildIKButton(service.getLocaleMessage(chatId, ADMIN_BT_SELECTED), SELECTED));
        keyboard.add(msgBuilder.buildIKRow(buttons));
        buttons.clear();

        List<Amenity> amenities = persistence.selectAllAmenities();
        TempAdminSettings adminSettings = persistence.selectTempAdminSettings(chatId);

        initSelectorPanel(chatId, amenities.size(), botConfig.getCountOfAppsOnPage(), adminSettings,
                PREVIOUS_PAGE_OF_AMENITIES, NEXT_PAGE_OF_AMENITIES, keyboard, buttons);

        for (int i = 0; i < botConfig.getCountOfAmenitiesOnPage(); i++) {

            if (adminSettings.getSelectedPage() + i >= amenities.size())
                break;

            buttons.add(msgBuilder.buildIKButton(service.getLocaleMessage(chatId, amenities.get(i).getLink()),
                    AMENITY + X + amenities.get(i).getIdAmenity()));
            keyboard.add(msgBuilder.buildIKRow(buttons));
            buttons.clear();
        }
    }

    protected void initBtChat(long chatId, List<InlineKeyboardRow> keyboard, List<InlineKeyboardButton> buttons) {

        int selectedApp = persistence.selectTempAdminSettings(chatId).getSelectedApplication();

        buttons.add(msgBuilder.buildIKButton(
                service.getLocaleMessage(chatId, ADMIN_BT_CHAT), OPEN_CHAT + X + selectedApp));
        keyboard.add(msgBuilder.buildIKRow(buttons));
        buttons.clear();
    }

    protected void initBtPreview(long chatId, List<InlineKeyboardRow> keyboard, List<InlineKeyboardButton> buttons) {
        TempNewApartment tempNewApartment = persistence.selectTempNewApartment(chatId);

        if (tempNewApartment.getNumber() != 0 &&
                tempNewApartment.getCountOfPictures() != 0 &&
                tempNewApartment.getArea() != 0 &&
                tempNewApartment.getLinksOfAmenities() != null) {

            buttons.add(msgBuilder.buildIKButton(service.getLocaleMessage(chatId, ADMIN_BT_PREVIEW), PREVIEW_APARTMENT));
            keyboard.add(msgBuilder.buildIKRow(buttons));
            buttons.clear();
        }
    }

    // actions

    public void nextPage(long chatId) {
        TempAdminSettings tempAdminSettings = persistence.selectTempAdminSettings(chatId);
        persistence.updateSelectedPageTempAdminSettings(chatId,
                tempAdminSettings.getSelectedPage() + botConfig.getCountOfAppsOnPage());
    }

    public void previousPage(long chatId) {
        TempAdminSettings tempAdminSettings = persistence.selectTempAdminSettings(chatId);
        persistence.updateSelectedPageTempAdminSettings(chatId,
                tempAdminSettings.getSelectedPage() - botConfig.getCountOfAppsOnPage());
    }

    // init

    public void initSelectorPanel(long chatId,
                                  int countOfEntity,
                                  int maxCountOfEntityOnPage,
                                  TempAdminSettings adminSettings,
                                  String dataOfPrevious,
                                  String dataOfNext,
                                  List<InlineKeyboardRow> keyboard,
                                  List<InlineKeyboardButton> buttons) {

        if (countOfEntity > maxCountOfEntityOnPage) {
            if (adminSettings.getSelectedPage() - maxCountOfEntityOnPage >= 0)
                buttons.add(msgBuilder.buildIKButton(
                        service.getLocaleMessage(chatId, "◀️"), dataOfPrevious));
            else
                buttons.add(msgBuilder.buildIKButton("🛑", EMPTY));

            if (adminSettings.getSelectedPage() + maxCountOfEntityOnPage < countOfEntity)
                buttons.add(msgBuilder.buildIKButton(
                        service.getLocaleMessage(chatId, "▶️"), dataOfNext));
            else
                buttons.add(msgBuilder.buildIKButton("🛑", EMPTY));

            keyboard.add(msgBuilder.buildIKRow(buttons));
            buttons.clear();
        }
    }

    // markups
}
