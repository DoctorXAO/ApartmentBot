package xao.develop.service.admin;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import xao.develop.config.AdminCommand;
import xao.develop.config.AdminMessageLink;
import xao.develop.config.GeneralCommand;
import xao.develop.model.BookingCard;
import xao.develop.model.TempAdminSettings;
import xao.develop.config.enums.TypeOfApp;
import xao.develop.config.enums.TypeOfAppStatus;
import xao.develop.service.BotMessage;

import java.util.List;

public abstract class AdminMessage extends BotMessage implements AdminCommand, AdminMessageLink, GeneralCommand {

    // setters

    // getters

    public int getCountOfArchive() {
        return persistence.selectAllBookingCardExceptWaiting().size();
    }

    public int getCountOfNewApps() {
        return persistence.selectBookingCardByStatus(TypeOfAppStatus.WAITING).size();
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

    public void updateBookingCardStatus(int idOfCard, TypeOfAppStatus status) {
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
                                 List<InlineKeyboardRow> keyboard,
                                 List<InlineKeyboardButton> buttons,
                                 TypeOfApp type) {

        List<BookingCard> bookingCards = null;
        String data = null;

        if (type.equals(TypeOfApp.APP)) {
            bookingCards = persistence.selectBookingCardByStatus(TypeOfAppStatus.WAITING);
            data = APP + X;
        }
        else if (type.equals(TypeOfApp.ARC)) {
            bookingCards = persistence.selectAllBookingCardExceptWaiting();
            data = ARC + X;
        }

        TempAdminSettings adminSettings = persistence.selectTempAdminSettings(chatId);

        if (bookingCards.size() > botConfig.getCountOfApps()) {
            if (adminSettings.getSelectedPage() - botConfig.getCountOfApps() >= 0)
                buttons.add(msgBuilder.buildIKButton(
                        service.getLocaleMessage(chatId, "◀️"), PREVIOUS_PAGE_OF_ARCHIVE));
            else
                buttons.add(msgBuilder.buildIKButton("🛑", EMPTY));

            if (adminSettings.getSelectedPage() + botConfig.getCountOfApps() < bookingCards.size())
                buttons.add(msgBuilder.buildIKButton(
                        service.getLocaleMessage(chatId, "▶️"), NEXT_PAGE_OF_ARCHIVE));
            else
                buttons.add(msgBuilder.buildIKButton("🛑", EMPTY));

            keyboard.add(msgBuilder.buildIKRow(buttons));
            buttons.clear();
        }

        if (!bookingCards.isEmpty())
            for (int i = 0; i < botConfig.getCountOfApps(); i++) {

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

    protected void initBtChat(long chatId,
                              List<InlineKeyboardRow> keyboard,
                              List<InlineKeyboardButton> buttons) {

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
                tempAdminSettings.getSelectedPage() + botConfig.getCountOfApps());
    }

    public void previousPage(long chatId) {
        TempAdminSettings tempAdminSettings = persistence.selectTempAdminSettings(chatId);
        persistence.updateSelectedPageTempAdminSettings(chatId,
                tempAdminSettings.getSelectedPage() - botConfig.getCountOfApps());
    }

    // markups
}
