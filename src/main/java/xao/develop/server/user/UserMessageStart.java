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
import xao.develop.server.BotMessage;
import xao.develop.server.MessageBuilder;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class UserMessageStart implements BotMessage, UserCommand {

    @Autowired
    BotConfig botConfig;

    @Autowired
    MessageBuilder msgBuilder;

    @Autowired
    UserLocalization userLoc;

    /** Получена команда /start **/
    @Override
    public Message sendMessage(Update update) throws TelegramApiException {
        log.trace("Method sendMessage(Update) started and finished");

        return botConfig.getTelegramClient().execute(msgBuilder.buildSendMessage(update,
                userLoc.getLocalizationText(update),
                getIKMarkup(update)));
    }

    @Override
    public InlineKeyboardMarkup getIKMarkup(Update update) {
        log.trace("Method getMainIKMarkup(Update) started");

        List<InlineKeyboardButton> buttons = new ArrayList<>();
        buttons.add(msgBuilder.buildIKButton(userLoc.getLocalizationButton(update, APARTMENTS), APARTMENTS));
        buttons.add(msgBuilder.buildIKButton(userLoc.getLocalizationButton(update, RENT_AN_APARTMENT), RENT_AN_APARTMENT));
        InlineKeyboardRow row1 = msgBuilder.buildIKRow(buttons);

        buttons.clear();
        buttons.add(msgBuilder.buildIKButton(userLoc.getLocalizationButton(update, HOUSE_INFORMATION), HOUSE_INFORMATION));
        buttons.add(msgBuilder.buildIKButton(userLoc.getLocalizationButton(update, CONTACTS), CONTACTS));
        InlineKeyboardRow row2 = msgBuilder.buildIKRow(buttons);

        buttons.clear();
        buttons.add(msgBuilder.buildIKButton(userLoc.getLocalizationButton(update, CHANGE_LANGUAGE), CHANGE_LANGUAGE));
        InlineKeyboardRow row3 = msgBuilder.buildIKRow(buttons);

        log.trace("Method getMainIKMarkup(Update) finished");

        return InlineKeyboardMarkup
                .builder()
                .keyboardRow(row1)
                .keyboardRow(row2)
                .keyboardRow(row3)
                .build();
    }
}
