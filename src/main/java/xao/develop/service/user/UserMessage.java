package xao.develop.service.user;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import xao.develop.config.UserCommand;
import xao.develop.config.UserMessageLink;
import xao.develop.service.BotMessage;

import java.util.ArrayList;
import java.util.List;

public abstract class UserMessage extends BotMessage implements UserCommand, UserMessageLink {

    // markups

    public InlineKeyboardMarkup getIKMarkupGotNewApp(long chatId) {
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
