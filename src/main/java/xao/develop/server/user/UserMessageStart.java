package xao.develop.server.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import xao.develop.config.BotConfig;
import xao.develop.config.UserCommand;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class UserMessageStart implements UserCommand {

    @Autowired
    BotConfig botConfig;

    @Autowired
    UserServer server;

    /** Получена команда /start **/
    public Message sendMessage(Update update) throws TelegramApiException {
        log.trace("Method cmd_start(Update) started");

        server.authorization(update.getMessage());

        log.trace("Method cmd_start(Update) finished");

        return botConfig.getTelegramClient().execute(server.buildSendMessage(update,
                server.getLocalizationText(update),
                getIKMarkup(update)));
    }

    public InlineKeyboardMarkup getIKMarkup(Update update) {
        log.trace("Method getMainIKMarkup(Update) started");

        List<InlineKeyboardButton> buttons = new ArrayList<>();
        buttons.add(server.buildIKButton(server.getLocalizationButton(update, APARTMENTS), APARTMENTS));
        buttons.add(server.buildIKButton(server.getLocalizationButton(update, RENT_AN_APARTMENT), RENT_AN_APARTMENT));
        InlineKeyboardRow row1 = server.buildIKRow(buttons);

        buttons.clear();
        buttons.add(server.buildIKButton(server.getLocalizationButton(update, HOUSE_INFORMATION), HOUSE_INFORMATION));
        buttons.add(server.buildIKButton(server.getLocalizationButton(update, CONTACTS), CONTACTS));
        InlineKeyboardRow row2 = server.buildIKRow(buttons);

        buttons.clear();
        buttons.add(server.buildIKButton(server.getLocalizationButton(update, CHANGE_LANGUAGE), CHANGE_LANGUAGE));
        InlineKeyboardRow row3 = server.buildIKRow(buttons);

        log.trace("Method getMainIKMarkup(Update) finished");

        return InlineKeyboardMarkup
                .builder()
                .keyboardRow(row1)
                .keyboardRow(row2)
                .keyboardRow(row3)
                .build();
    }
}
