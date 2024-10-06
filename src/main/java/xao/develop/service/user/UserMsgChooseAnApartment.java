package xao.develop.service.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.*;

@Slf4j
@Service
public class UserMsgChooseAnApartment extends UserMessage {

    @Override
    public InlineKeyboardMarkup getIKMarkup(long chatId) {
        List<InlineKeyboardRow> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> buttons = new ArrayList<>();

        int selector = persistence.selectTempApartmentSelector(chatId).getSelector();

        if (selector > 0)
            buttons.add(msgBuilder.buildIKButton("◀️", RAA_PREVIOUS_APARTMENT));
        else
            buttons.add(msgBuilder.buildIKButton("🛑", EMPTY));

        if (selector < persistence.selectAllFreeApartments(chatId).size() - 1)
            buttons.add(msgBuilder.buildIKButton("▶️", RAA_NEXT_APARTMENT));
        else
            buttons.add(msgBuilder.buildIKButton("🛑", EMPTY));

        keyboard.add(msgBuilder.buildIKRow(buttons));
        buttons.clear();

        buttons.add(msgBuilder.buildIKButton(service.getLocaleMessage(chatId, USER_BT_BOOK), RAA_BOOK));
        keyboard.add(msgBuilder.buildIKRow(buttons));
        buttons.clear();

        buttons.add(msgBuilder.buildIKButton(service.getLocaleMessage(chatId, GENERAL_BT_BACK),
                RAA_QUIT_FROM_CHOOSER_AN_APARTMENT));
        keyboard.add(msgBuilder.buildIKRow(buttons));

        return InlineKeyboardMarkup
                .builder()
                .keyboard(keyboard)
                .build();
    }
}
