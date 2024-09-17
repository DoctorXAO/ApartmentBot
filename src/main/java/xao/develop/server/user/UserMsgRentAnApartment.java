package xao.develop.server.user;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

@Service
public class UserMsgRentAnApartment extends UserMsg {

    @Override
    public InlineKeyboardMarkup getIKMarkup(Update update) {
        return InlineKeyboardMarkup
                .builder()
                .keyboardRow(new InlineKeyboardRow(msgBuilder.buildIKButton(
                        userLoc.getLocalizationButton(update, CHOOSE_AN_APARTMENT), CHOOSE_AN_APARTMENT)))
                .keyboardRow(new InlineKeyboardRow(msgBuilder.buildIKButton(
                        userLoc.getLocalizationButton(update, BACK), BACK_TO_START)))
                .build();
    }
}
