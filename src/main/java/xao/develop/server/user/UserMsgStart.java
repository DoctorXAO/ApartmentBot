package xao.develop.server.user;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserMsgStart extends UserMsg {

    @Override
    public InlineKeyboardMarkup getIKMarkup(Update update) {
        List<InlineKeyboardButton> buttons = new ArrayList<>();
        buttons.add(msgBuilder.buildIKButton(userLoc.getLocalizationButton(update, APARTMENTS), APARTMENTS));
        buttons.add(msgBuilder.buildIKButton(userLoc.getLocalizationButton(update, RAA_RENT_AN_APARTMENT), RAA_RENT_AN_APARTMENT));
        InlineKeyboardRow row1 = msgBuilder.buildIKRow(buttons);

        buttons.clear();
        buttons.add(msgBuilder.buildIKButton(userLoc.getLocalizationButton(update, HI_HOUSE_INFORMATION), HI_HOUSE_INFORMATION));
        buttons.add(msgBuilder.buildIKButton(userLoc.getLocalizationButton(update, CONTACTS), CONTACTS));
        InlineKeyboardRow row2 = msgBuilder.buildIKRow(buttons);

        buttons.clear();
        buttons.add(msgBuilder.buildIKButton(userLoc.getLocalizationButton(update, CHANGE_LANGUAGE), CHANGE_LANGUAGE));
        InlineKeyboardRow row3 = msgBuilder.buildIKRow(buttons);

        return InlineKeyboardMarkup
                .builder()
                .keyboardRow(row1)
                .keyboardRow(row2)
                .keyboardRow(row3)
                .build();
    }
}
