package xao.develop.service.admin;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import xao.develop.config.AdminCommand;
import xao.develop.config.AdminMessageLink;
import xao.develop.config.GeneralCommand;
import xao.develop.model.BookingCard;
import xao.develop.model.TempAdminSettings;
import xao.develop.config.enums.App;
import xao.develop.config.enums.AppStatus;
import xao.develop.service.BotMessage;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
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

    // creates

    public void createAdminSettings(long chatId) {
        persistence.insertTempAdminSettings(chatId);
    }

    // updates

    public void updateBookingCardStatus(int idOfCard, AppStatus status) {
        persistence.updateBookingCard(idOfCard, status);
    }

    public void updateAdminSettings(long chatId, int selectedApp) {
        persistence.updateSelectedAppTempAdminSettings(chatId, selectedApp);
    }

    // deletes

    public void deleteAdminSettings(long chatId) {
        persistence.deleteTempAdminSettings(chatId);
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

                if (i >= bookingCards.size())
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
            ClassLoader classLoader = getClass().getClassLoader();
            URL resource = classLoader.getResource("img/apartments");

            if (resource == null)
                throw new Exception("Directory with photos isn't found!");

            String[] fileNames = new File(resource.getFile()).list();

            if (fileNames != null) {
                Arrays.sort(fileNames, String::compareToIgnoreCase);

                TempAdminSettings adminSettings = persistence.selectTempAdminSettings(chatId);

                initSelectorPanel(chatId, fileNames.length, botConfig.getCountOfAppsOnPage(), adminSettings,
                        PREVIOUS_PAGE_OF_APART, NEXT_PAGE_OF_APART, keyboard, buttons);

                for (int i = 0; i < botConfig.getCountOfApartmentOnPage(); i++) {

                    if (i >= fileNames.length)
                        break;

                    buttons.add(msgBuilder.buildIKButton(service.getLocaleMessage(chatId,
                            ADMIN_BT_APARTMENT,
                            fileNames[i]), APARTMENT + X + fileNames[1]));
                    keyboard.add(msgBuilder.buildIKRow(buttons));
                    buttons.clear();
                }
            }
        } catch (Exception ex) {
            log.debug("initSelectorApartments exception: {}", ex.getMessage());
        }
    }

    protected void initBtChat(long chatId, List<InlineKeyboardRow> keyboard, List<InlineKeyboardButton> buttons) {

        int selectedApp = persistence.selectTempAdminSettings(chatId).getSelectedApplication();

        buttons.add(msgBuilder.buildIKButton(
                service.getLocaleMessage(chatId, ADMIN_BT_CHAT), OPEN_CHAT + X + selectedApp));
        keyboard.add(msgBuilder.buildIKRow(buttons));
        buttons.clear();
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
                                  int countOfEntityOnPage,
                                  int maxCountOfEntityOnPage,
                                  TempAdminSettings adminSettings,
                                  String dataOfPrevious,
                                  String dataOfNext,
                                  List<InlineKeyboardRow> keyboard,
                                  List<InlineKeyboardButton> buttons) {

        if (countOfEntityOnPage > maxCountOfEntityOnPage) {
            if (adminSettings.getSelectedPage() - maxCountOfEntityOnPage >= 0)
                buttons.add(msgBuilder.buildIKButton(
                        service.getLocaleMessage(chatId, "◀️"), dataOfPrevious));
            else
                buttons.add(msgBuilder.buildIKButton("🛑", EMPTY));

            if (adminSettings.getSelectedPage() + maxCountOfEntityOnPage < countOfEntityOnPage)
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
