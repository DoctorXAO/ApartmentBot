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
        return persistence.selectAdminSettings(chatId).getSelectedApplication();
    }

    public boolean isCheckingSelectedAmenities(long chatId) {
        return persistence.selectAdminSettings(chatId).isCheckingSelectedAmenities();
    }

    public Object[] getTempNewApartmentParameters(long chatId) {
        TempNewApartment newApartment = persistence.selectTempNewApartment(chatId);

        List<Amenity> selectedAmenities = getSelectedAmenities(chatId);

        StringBuilder amenities = convertAmenityLinksToString(chatId, selectedAmenities.stream()
                .map(Amenity::getLink)
                .toList());

        return new Object[]{
                newApartment.getNumber(),
                newApartment.getCountOfPictures(),
                newApartment.getArea(),
                amenities};
    }

    public StringBuilder getStringOfSelectedAmenities(long chatId) {
        List<Amenity> selectedAmenities = getSelectedAmenities(chatId);

        return convertAmenityLinksToString(chatId, selectedAmenities.stream()
                .map(Amenity::getLink)
                .toList());
    }

    public Object[] getPreviewApartmentParameters(long chatId) {
        TempNewApartment newApartment = persistence.selectTempNewApartment(chatId);

        List<Amenity> selectedAmenities = getSelectedAmenities(chatId);

        StringBuilder amenities = convertAmenityLinksToString(chatId, selectedAmenities.stream()
                .map(Amenity::getLink)
                .toList());

        return new Object[]{
                newApartment.getArea(),
                amenities};
    }

    public Object[] getEditApartmentParameters(long chatId, String numberOfApartment) {
        Apartment apartment = persistence.selectApartment(Integer.parseInt(numberOfApartment));

        StringBuilder amenities = convertAmenityLinksToString(chatId,
                apartment.getAmenities().stream().map(Amenity::getLink).toList());

        return new Object[]{
                apartment.getNumber(),
                apartment.getArea(),
                amenities
        };
    }

    public Object[] getListOfAmenitiesPages(long chatId) {
        return new Object[]{getSelectedPage(chatId), getAmenities().size()};
    }

    public boolean isNewApartment(long chatId) {
        return persistence.selectAdminSettings(chatId).isNewApartment();
    }

    public boolean isEditingPhotos(long chatId) {
        return persistence.selectAdminSettings(chatId).isEditingPhotos();
    }

    public boolean isEditingApartments(long chatId) {
        return persistence.selectAdminSettings(chatId).isEditingApartment();
    }

    public boolean isEditingAmenity(long chatId) {
        return persistence.selectAdminSettings(chatId).isEditingAmenity();
    }

    public TempNewApartment getTempNewApartment(long chatId) {
        return persistence.selectTempNewApartment(chatId);
    }

    public List<Amenity> getSelectedAmenities(long chatId) {
        return persistence.selectAllSelectedAmenities(chatId);
    }

    public int getSelectedApartment(long chatId) {
        return persistence.selectAdminSettings(chatId).getSelectedApartment();
    }

    public int getSelectedAmenity(long chatId) {
        return persistence.selectAdminSettings(chatId).getSelectedAmenity();
    }

    public int getSelectedPage(long chatId) {
        return persistence.selectAdminSettings(chatId).getSelectedPage();
    }

    public Amenity getAmenityById(int idOfAmenity) {
        return amenityPersistence.select(idOfAmenity);
    }

    public List<Amenity> getAmenitiesOfApartment(long chatId) {
        return persistence.selectApartment(getSelectedApartment(chatId)).getAmenities();
    }

    public List<Amenity> getAmenities() {
        return amenityPersistence.select();
    }

    // creates

    public void createAdminSettings(long chatId) {
        persistence.insertAdminSettings(chatId);
    }

    public void createNewApartmentField(long chatId) {
        persistence.insertTempNewApartment(chatId);
    }

    // inserts

    public void insertApartment(int number, double area, List<Amenity> amenities) {
        persistence.insertApartment(number, area, amenities);
    }

    public void insertSelectedAmenity(long chatId, Amenity amenity) {
        persistence.insertTempSelectedAmenity(chatId, amenity);
    }

    // updates

    public void updateBookingCardStatus(int idOfCard, AppStatus status) {
        persistence.updateBookingCard(idOfCard, status);
    }

    public void updateSelectedAppAdminSettings(long chatId, int selectedApp) {
        persistence.updateSelectedAppAdminSettings(chatId, selectedApp);
    }

    public void updateSelectedPageAdminSettings(long chatId, int selectedPage) {
        persistence.updateSelectedPageAdminSettings(chatId, selectedPage);
    }

    public void updateSelectedAmenityAdminSettings(long chatId, int selectedAmenity) {
        persistence.updateSelectedAmenityAdminSettings(chatId, selectedAmenity);
    }

    public void updateNewApartmentAdminSettings(long chatId, boolean isNewApartment) {
        persistence.updateNewApartmentAdminSettings(chatId, isNewApartment);
    }

    public void updateCheckingSelectedAmenitiesAdminSettings(long chatId, boolean isCheckingSelectedAmenities) {
        persistence.updateCheckingSelectedAmenitiesAdminSettings(chatId, isCheckingSelectedAmenities);
    }

    public void updateEditingApartment(long chatId, boolean isEditingApartment) {
        persistence.updateEditingApartmentAdminSettings(chatId, isEditingApartment);
    }

    public void updateEditingPhotos(long chatId, boolean isEditingPhotos) {
        persistence.updateEditingPhotosAdminSettings(chatId, isEditingPhotos);
    }

    public void updateEditingAmenity(long chatId, boolean isEditingAmenity) {
        persistence.updateEditingAmenityAdminSettings(chatId, isEditingAmenity);
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

    public void updateSelectedApartment(long chatId, int selectedApartment) {
        persistence.updateSelectedApartmentAdminSettings(chatId, selectedApartment);
    }

    public void updateNumberApartment(int number, int newNumber) {
        persistence.updateNumberApartment(number, newNumber);
    }
    
    public void updateAreaApartment(int number, double area) {
        persistence.updateAreaApartment(number, area);
    }

    public void updateAmenitiesOfApartment(int number, List<Amenity> amenities) {
        persistence.updateAmenitiesApartment(number, amenities);
    }

    // deletes

    public void deleteTempNewApartment(long chatId) {
        persistence.deleteTempNewApartment(chatId);
    }

    public void deleteSelectedAmenity(long chatId, Amenity amenity) {
        persistence.deleteTempSelectedAmenity(chatId, amenity);
    }

    public void deleteSelectedAmenities(long chatId) {
        persistence.deleteAllTempSelectedAmenity(chatId);
    }

    public void deleteApartment(int numberOfApartment) {
        persistence.deleteApartment(numberOfApartment);
    }

    // init

    protected void initSelectorApps(long chatId,
                                    App type,
                                    List<InlineKeyboardRow> keyboard,
                                    List<InlineKeyboardButton> buttons) {

        List<BookingCard> bookingCards = null;
        String data = EMPTY;

        AdminSettings adminSettings = persistence.selectAdminSettings(chatId);

        if (type.equals(App.APP)) {
            bookingCards = persistence.selectBookingCardByStatus(AppStatus.WAITING);
            data = APP + X;
            initSelectorPanel(chatId, bookingCards.size(), botConfig.getCountOfEntityOnPage(), adminSettings,
                    PREVIOUS_PAGE_OF_NEW_APPS, NEXT_PAGE_OF_NEW_APPS, keyboard, buttons);
        }
        else if (type.equals(App.ARC)) {
            bookingCards = persistence.selectAllBookingCardExceptWaiting();
            data = ARC + X;
            initSelectorPanel(chatId, bookingCards.size(), botConfig.getCountOfEntityOnPage(), adminSettings,
                    PREVIOUS_PAGE_OF_ARCHIVE, NEXT_PAGE_OF_ARCHIVE, keyboard, buttons);
        }

        if (!bookingCards.isEmpty())
            for (int i = 0; i < botConfig.getCountOfEntityOnPage(); i++) {

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

            log.debug("Size of the list of the apartments is {}", apartments.size());

            AdminSettings adminSettings = persistence.selectAdminSettings(chatId);

            initSelectorPanel(chatId, apartments.size(), botConfig.getCountOfEntityOnPage(), adminSettings,
                    PREVIOUS_PAGE_OF_APART, NEXT_PAGE_OF_APART, keyboard, buttons);

            for (int i = 0; i < botConfig.getCountOfEntityOnPage(); i++) {

                if (adminSettings.getSelectedPage() + i >= apartments.size())
                    break;

                int number = adminSettings.getSelectedPage() + i;

                buttons.add(msgBuilder.buildIKButton(service.getLocaleMessage(chatId,
                        ADMIN_BT_APARTMENT,
                        apartments.get(number).getNumber()),
                        APARTMENT + X + apartments.get(number).getNumber()));
                keyboard.add(msgBuilder.buildIKRow(buttons));
                buttons.clear();
            }
        } catch (Exception ex) {
            log.debug("initSelectorApartments exception: {}", ex.getMessage());
        }
    }

    protected void initNASelectorAmenities(long chatId,
                                           List<InlineKeyboardRow> keyboard,
                                           List<InlineKeyboardButton> buttons) {

        AdminSettings adminSettings = persistence.selectAdminSettings(chatId);

        if (adminSettings.isCheckingSelectedAmenities())
            initSelectorSelectedAmenities(chatId, adminSettings, PREVIOUS_PAGE_OF_SL_AM, NEXT_PAGE_OF_SL_AM, keyboard, buttons);
        else
            initSelectorAvailableAmenities(chatId, adminSettings, PREVIOUS_PAGE_OF_AV_AM, NEXT_PAGE_OF_AV_AM, keyboard, buttons);
    }

    protected void initEditSelectorAmenities(long chatId,
                                           List<InlineKeyboardRow> keyboard,
                                           List<InlineKeyboardButton> buttons) {

        AdminSettings adminSettings = persistence.selectAdminSettings(chatId);

        if (adminSettings.isCheckingSelectedAmenities())
            initSelectorSelectedAmenities(chatId, adminSettings, PREVIOUS_PAGE_OF_SL_EAM, NEXT_PAGE_OF_SL_EAM, keyboard, buttons);
        else
            initSelectorAvailableAmenities(chatId, adminSettings, PREVIOUS_PAGE_OF_AV_EAM, NEXT_PAGE_OF_AV_EAM, keyboard, buttons);
    }

    private void initSelectorAvailableAmenities(long chatId,
                                                AdminSettings adminSettings,
                                                String dataOfPrevious,
                                                String dataOfNext,
                                                List<InlineKeyboardRow> keyboard,
                                                List<InlineKeyboardButton> buttons) {

        buttons.add(msgBuilder.buildIKButton(service.getLocaleMessage(chatId, ADMIN_BT_SELECTED), SELECTED));
        keyboard.add(msgBuilder.buildIKRow(buttons));
        buttons.clear();

        List<Amenity> amenities = amenityPersistence.select();
        List<Amenity> selectedAmenities = persistence.selectAllSelectedAmenities(chatId);

        for (Amenity selectedAmenity : selectedAmenities)
            amenities.removeIf(amenity -> amenity.getLink().equals(selectedAmenity.getLink()));

        initSelectorPanel(chatId, amenities.size(), botConfig.getCountOfEntityOnPage(), adminSettings,
                dataOfPrevious, dataOfNext, keyboard, buttons);

        for (int i = 0; i < botConfig.getCountOfEntityOnPage(); i++) {

            if (adminSettings.getSelectedPage() + i >= amenities.size())
                break;

            buttons.add(msgBuilder.buildIKButton(service.getLocaleMessage(chatId,
                            amenities.get(adminSettings.getSelectedPage() + i).getLink()),
                    AMENITY + X + amenities.get(adminSettings.getSelectedPage() + i).getIdAmenity()));
            keyboard.add(msgBuilder.buildIKRow(buttons));
            buttons.clear();
        }
    }

    private void initSelectorSelectedAmenities(long chatId,
                                               AdminSettings adminSettings,
                                               String dataOfPrevious,
                                               String dataOfNext,
                                               List<InlineKeyboardRow> keyboard,
                                               List<InlineKeyboardButton> buttons) {

        buttons.add(msgBuilder.buildIKButton(service.getLocaleMessage(chatId, ADMIN_BT_AVAILABLE), AVAILABLE));
        keyboard.add(msgBuilder.buildIKRow(buttons));
        buttons.clear();

        List<Amenity> selectedAmenities = persistence.selectAllSelectedAmenities(chatId);

        initSelectorPanel(chatId, selectedAmenities.size(), botConfig.getCountOfEntityOnPage(), adminSettings,
                dataOfPrevious, dataOfNext, keyboard, buttons);

        for (int i = 0; i < botConfig.getCountOfEntityOnPage(); i++) {

            if (adminSettings.getSelectedPage() + i >= selectedAmenities.size())
                break;

            Amenity amenity = selectedAmenities.get(adminSettings.getSelectedPage() + i);

            buttons.add(msgBuilder.buildIKButton(service.getLocaleMessage(chatId,
                            amenity.getLink()),
                    AMENITY + X + amenity.getIdAmenity()));
            keyboard.add(msgBuilder.buildIKRow(buttons));
            buttons.clear();
        }
    }

    protected void initListOfAmenities(long chatId,
                                     List<InlineKeyboardRow> keyboard,
                                     List<InlineKeyboardButton> buttons) {
        AdminSettings adminSettings = persistence.selectAdminSettings(chatId);
        List<Amenity> amenities = amenityPersistence.select();

        initSelectorPanel(chatId, amenities.size(), botConfig.getCountOfEntityOnPage(), adminSettings,
                PREVIOUS_PAGE_OF_LS_AM, NEXT_PAGE_OF_LS_AM, keyboard, buttons);

        for (int i = 0; i < botConfig.getCountOfEntityOnPage(); i++) {

            if (adminSettings.getSelectedPage() + i >= amenities.size())
                break;

            buttons.add(msgBuilder.buildIKButton(service.getLocaleMessage(chatId,
                            amenities.get(adminSettings.getSelectedPage() + i).getLink()),
                    ED_AMENITY + X + amenities.get(adminSettings.getSelectedPage() + i).getIdAmenity()));
            keyboard.add(msgBuilder.buildIKRow(buttons));
            buttons.clear();
        }
    }

    protected void initBtChat(long chatId, List<InlineKeyboardRow> keyboard, List<InlineKeyboardButton> buttons) {

        int selectedApp = persistence.selectAdminSettings(chatId).getSelectedApplication();

        buttons.add(msgBuilder.buildIKButton(
                service.getLocaleMessage(chatId, ADMIN_BT_CHAT), OPEN_CHAT + X + selectedApp));
        keyboard.add(msgBuilder.buildIKRow(buttons));
        buttons.clear();
    }

    protected void initBtPreview(long chatId, List<InlineKeyboardRow> keyboard, List<InlineKeyboardButton> buttons) {
        TempNewApartment tempNewApartment = persistence.selectTempNewApartment(chatId);
        List<Amenity> selectedAmenities = persistence.selectAllSelectedAmenities(chatId);

        if (tempNewApartment.getNumber() != 0 &&
                tempNewApartment.getCountOfPictures() != 0 &&
                tempNewApartment.getArea() != 0 &&
                !selectedAmenities.isEmpty()) {

            buttons.add(msgBuilder.buildIKButton(service.getLocaleMessage(chatId, ADMIN_BT_PREVIEW), PREVIEW_APARTMENT));
            keyboard.add(msgBuilder.buildIKRow(buttons));
            buttons.clear();
        }
    }

    protected void initBtCreateNewAmenity(long chatId, List<InlineKeyboardRow> keyboard, List<InlineKeyboardButton> buttons) {
        TempNewAmenity newAmenity = tempNewAmenityPersistence.select(chatId);

        if (newAmenity.getLink() != null &&
                newAmenity.getEn() != null &&
                newAmenity.getTr() != null &&
                newAmenity.getRu() != null &&
                newAmenity.getImportance() != 0)
        {
            buttons.add(msgBuilder.buildIKButton(service.getLocaleMessage(chatId, ADMIN_BT_CREATE), CREATE_NEW_AMENITY));
            keyboard.add(msgBuilder.buildIKRow(buttons));
            buttons.clear();
        }
    }

    public void initSelectorPanel(long chatId,
                                  int countOfEntity,
                                  int maxCountOfEntityOnPage,
                                  AdminSettings adminSettings,
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

    // actions

    public void nextPage(long chatId) {
        AdminSettings adminSettings = persistence.selectAdminSettings(chatId);

        int selectedPage = adminSettings.getSelectedPage() + botConfig.getCountOfEntityOnPage();

        updateSelectedPageAdminSettings(chatId, selectedPage);
    }

    public void previousPage(long chatId) {
        AdminSettings adminSettings = persistence.selectAdminSettings(chatId);

        int selectedPage = adminSettings.getSelectedPage() - botConfig.getCountOfEntityOnPage();

        updateSelectedPageAdminSettings(chatId, selectedPage);
    }

    private StringBuilder convertAmenityLinksToString(long chatId, List<String> amenityLinks) {
        StringBuilder amenities = new StringBuilder();

        for(String link : amenityLinks)
            amenities.append(service.getLocaleMessage(chatId, link)).append("\n");

        return amenities;
    }
}
