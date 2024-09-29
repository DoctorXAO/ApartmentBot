package xao.develop.server.user;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

@Service
public class UserMsgChangeLanguage extends UserMsg {

    @Override
    public InlineKeyboardMarkup getIKMarkup(Update update) {
        return InlineKeyboardMarkup
                .builder()
                .keyboardRow(new InlineKeyboardRow(msgBuilder.buildIKButton(
                        server.getLocaleMessage(update, USER_BT_BACK), BACK_TO_START)))
                .keyboardRow(new InlineKeyboardRow(
                        msgBuilder.buildIKButton("\uD83C\uDDF9\uD83C\uDDF7 Türkçe", TR)))
                .keyboardRow(new InlineKeyboardRow(
                        msgBuilder.buildIKButton("\uD83C\uDDEC\uD83C\uDDE7 English", EN)))
                .keyboardRow(new InlineKeyboardRow(
                        msgBuilder.buildIKButton("\uD83C\uDDF7\uD83C\uDDFA Русский", RU)))
                .build();
    }
}
