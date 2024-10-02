package xao.develop.service.admin;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import xao.develop.model.BookingCard;
import xao.develop.service.BookingCardStatus;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class AdminMsgApplication extends AdminMessage {

    public Object[] getParameters(Long id) {
        BookingCard bookingCard = persistence.selectBookingCardById(id);

        return new Object[]{
                bookingCard.getId(),
                bookingCard.getNumberOfApartment(),
                service.getCheckDate(bookingCard.getChatId()),
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

    @Override
    protected InlineKeyboardMarkup getIKMarkup(Update update) {
        List<InlineKeyboardRow> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> buttons = new ArrayList<>();

        buttons.add(msgBuilder.buildIKButton(service.getLocaleMessage(update, ADMIN_BT_BACK), NEW_APPLICATIONS));
        keyboard.add(msgBuilder.buildIKRow(buttons));
        buttons.clear();

        return InlineKeyboardMarkup
                .builder()
                .keyboard(keyboard)
                .build();
    }
}
