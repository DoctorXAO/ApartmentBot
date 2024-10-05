package xao.develop.service.admin;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import xao.develop.config.AdminCommand;
import xao.develop.config.AdminMessageLink;
import xao.develop.model.BookingCard;
import xao.develop.model.TempAdminSettings;
import xao.develop.config.enums.TypeOfApp;
import xao.develop.config.enums.TypeOfAppStatus;
import xao.develop.service.BotMessage;

import java.util.ArrayList;
import java.util.List;

public abstract class AdminMessage extends BotMessage implements AdminCommand, AdminMessageLink {

    // setters

    // getters

    public int getCountOfArchive() {
        return persistence.selectAllBookingCardExceptWaiting().size();
    }

    public int getCountOfNewApps() {
        return persistence.selectBookingCardByStatus(TypeOfAppStatus.WAITING).size();
    }

    public Object[] getAppParameters(Update update, int idOfCard) {
        BookingCard bookingCard = persistence.selectBookingCard(idOfCard);

        String status;
            if (bookingCard.getStatus().equals(TypeOfAppStatus.WAITING.getType()))
                status = service.getLocaleMessage(service.getChatId(update), ADMIN_MSG_STATUS_WAITING);
            else if (bookingCard.getStatus().equals(TypeOfAppStatus.ACCEPTED.getType()))
                status = service.getLocaleMessage(service.getChatId(update), ADMIN_MSG_STATUS_ACCEPTED);
            else if (bookingCard.getStatus().equals(TypeOfAppStatus.DENIED.getType()))
                status = service.getLocaleMessage(service.getChatId(update), ADMIN_MSG_STATUS_DENIED);
            else if (bookingCard.getStatus().equals(TypeOfAppStatus.FINISHED.getType()))
                status = service.getLocaleMessage(service.getChatId(update), ADMIN_MSG_STATUS_FINISHED);
            else
                status = "null";

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

    protected void initSelectorApps(Update update,
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

        TempAdminSettings adminSettings = persistence.selectTempAdminSettings(service.getChatId(update));

        if (bookingCards.size() > botConfig.getCountOfApps()) {
            if (adminSettings.getSelectedPage() - botConfig.getCountOfApps() >= 0)
                buttons.add(msgBuilder.buildIKButton(
                        service.getLocaleMessage(service.getChatId(update), "◀️"), PREVIOUS_PAGE_OF_ARCHIVE));
            else
                buttons.add(msgBuilder.buildIKButton("🛑", EMPTY));

            if (adminSettings.getSelectedPage() + botConfig.getCountOfApps() < bookingCards.size())
                buttons.add(msgBuilder.buildIKButton(
                        service.getLocaleMessage(service.getChatId(update), "▶️"), NEXT_PAGE_OF_ARCHIVE));
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

                buttons.add(msgBuilder.buildIKButton(service.getLocaleMessage(service.getChatId(update),
                                GENERAL_BT_APP,
                                bookingCard.getId(),
                                statusIcon),
                        data + bookingCard.getId()));
                keyboard.add(msgBuilder.buildIKRow(buttons));
                buttons.clear();
            }
    }

    protected void initBtChat(Update update,
                           List<InlineKeyboardRow> keyboard,
                           List<InlineKeyboardButton> buttons) {

        buttons.add(msgBuilder.buildIKButton(
                service.getLocaleMessage(service.getChatId(update), ADMIN_BT_CHAT), OPEN_CHAT));
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

    public InlineKeyboardMarkup getIKMarkupUpdatedStatus(long chatId) {
        List<InlineKeyboardRow> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> buttons = new ArrayList<>();

        buttons.add(msgBuilder.buildIKButton(service.getLocaleMessage(chatId, GENERAL_BT_OK), DELETE));
        keyboard.add(msgBuilder.buildIKRow(buttons));

        return InlineKeyboardMarkup
                .builder()
                .keyboard(keyboard)
                .build();
    }
}
