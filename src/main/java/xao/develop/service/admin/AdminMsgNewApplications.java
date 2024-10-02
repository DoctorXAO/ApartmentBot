package xao.develop.service.admin;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import xao.develop.model.BookingCard;
import xao.develop.service.BookingCardStatus;

import java.util.ArrayList;
import java.util.List;

@Service
public class AdminMsgNewApplications extends AdminMessage {

    public int getCountOfNewApplications() {
        return persistence.selectBookingCardByStatus(BookingCardStatus.WAITING).size();
    }

    public void createAdminSettings(long chatId) {
        persistence.insertTempAdminSettings(chatId);
    }

    public void deleteAdminSettings(long chatId) {
        persistence.deleteTempAdminSettings(chatId);
    }

    @Override
    protected InlineKeyboardMarkup getIKMarkup(Update update) {
        List<InlineKeyboardRow> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> buttons = new ArrayList<>();

        List<BookingCard> bookingCards = persistence.selectBookingCardByStatus(BookingCardStatus.WAITING);

        for (BookingCard bookingCard : bookingCards) {
            buttons.add(msgBuilder.buildIKButton(service.getLocaleMessage(update, APPLICATION, bookingCard.getId()),
                    APP + bookingCard.getId()));
            keyboard.add(msgBuilder.buildIKRow(buttons));
            buttons.clear();
        }

        buttons.add(msgBuilder.buildIKButton(service.getLocaleMessage(update, ADMIN_BT_BACK), BACK_TO_START));
        keyboard.add(msgBuilder.buildIKRow(buttons));
        buttons.clear();

        return InlineKeyboardMarkup
                .builder()
                .keyboard(keyboard)
                .build();
    }
}
