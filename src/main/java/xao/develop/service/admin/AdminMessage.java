package xao.develop.service.admin;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import xao.develop.config.AdminCommand;
import xao.develop.config.AdminMessageLink;
import xao.develop.model.BookingCard;
import xao.develop.model.TempAdminSettings;
import xao.develop.config.enums.TypeOfApp;
import xao.develop.config.enums.TypesOfAppStatus;
import xao.develop.service.BotMessage;

import java.util.List;

public abstract class AdminMessage extends BotMessage implements AdminCommand, AdminMessageLink {

    // setters

    // getters

    public int getCountOfArchive() {
        return persistence.selectAllBookingCardExceptWaiting().size();
    }

    public int getCountOfNewApps() {
        return persistence.selectBookingCardByStatus(TypesOfAppStatus.WAITING).size();
    }

    public Object[] getAppParameters(int idOfCard) {
        BookingCard bookingCard = persistence.selectBookingCard(idOfCard);

        return new Object[]{
                bookingCard.getId(),
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

    // creates

    public void createAdminSettings(long chatId) {
        persistence.insertTempAdminSettings(chatId);
    }

    // updates

    public void updateBookingCardStatus(int idOfCard, TypesOfAppStatus status) {
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
            bookingCards = persistence.selectBookingCardByStatus(TypesOfAppStatus.WAITING);
            data = APP + X;
        }
        else if (type.equals(TypeOfApp.ARC)) {
            bookingCards = persistence.selectAllBookingCardExceptWaiting();
            data = ARC + X;
        }

        TempAdminSettings adminSettings = persistence.selectTempAdminSettings(service.getChatId(update));

        if (bookingCards.size() > botConfig.getCountOfApps()) {
            if (adminSettings.getSelectedPage() - botConfig.getCountOfApps() >= 0)
                buttons.add(msgBuilder.buildIKButton(service.getLocaleMessage(update, "◀️"), PREVIOUS_PAGE_OF_ARCHIVE));
            else
                buttons.add(msgBuilder.buildIKButton("🛑", EMPTY));

            if (adminSettings.getSelectedPage() + botConfig.getCountOfApps() < bookingCards.size())
                buttons.add(msgBuilder.buildIKButton(service.getLocaleMessage(update, "▶️"), NEXT_PAGE_OF_ARCHIVE));
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
                buttons.add(msgBuilder.buildIKButton(service.getLocaleMessage(update, APPLICATION, bookingCard.getId()),
                        data + bookingCard.getId()));
                keyboard.add(msgBuilder.buildIKRow(buttons));
                buttons.clear();
            }
    }

    protected void initBtChat(Update update,
                           List<InlineKeyboardRow> keyboard,
                           List<InlineKeyboardButton> buttons) {

        buttons.add(msgBuilder.buildIKButton(service.getLocaleMessage(update, ADMIN_BT_CHAT), OPEN_CHAT));
        keyboard.add(msgBuilder.buildIKRow(buttons));
        buttons.clear();
    }

    // other

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
}
