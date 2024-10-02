package xao.develop.service.admin;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import xao.develop.model.BookingCard;
import xao.develop.model.TempAdminSettings;
import xao.develop.service.BookingCardStatus;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class AdminMsgApplication extends AdminMessage {

    public Object[] getParameters(Long id) {
        BookingCard bookingCard = persistence.selectBookingCard(id);

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

    public void updateBookingCardStatus(Long id, BookingCardStatus status) {
        persistence.updateBookingCard(id, status);
    }

    public void updateAdminSettings(long chatId, int selectedApartment) {
        persistence.updateTempAdminSettings(chatId, selectedApartment);
    }

    @Override
    protected InlineKeyboardMarkup getIKMarkup(Update update) {
        List<InlineKeyboardRow> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> buttons = new ArrayList<>();

        int selectedApp = persistence.selectTempAdminSettings(service.getChatId(update)).getSelectedApplication();

        buttons.add(msgBuilder.buildIKButton(service.getLocaleMessage(update, ADMIN_BT_REFUSE), REFUSE_APP + selectedApp));
        buttons.add(msgBuilder.buildIKButton(service.getLocaleMessage(update, ADMIN_BT_ACCEPT), ACCEPT_APP + selectedApp));
        keyboard.add(msgBuilder.buildIKRow(buttons));
        buttons.clear();

        buttons.add(msgBuilder.buildIKButton(service.getLocaleMessage(update, ADMIN_BT_CHAT), NEW_APPLICATIONS));
        keyboard.add(msgBuilder.buildIKRow(buttons));
        buttons.clear();

        buttons.add(msgBuilder.buildIKButton(service.getLocaleMessage(update, ADMIN_BT_BACK), NEW_APPLICATIONS));
        keyboard.add(msgBuilder.buildIKRow(buttons));
        buttons.clear();

        return InlineKeyboardMarkup
                .builder()
                .keyboard(keyboard)
                .build();
    }
}
