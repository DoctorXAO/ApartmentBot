package xao.develop.service.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import xao.develop.config.UserCommand;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class UserMainIK implements UserCommand {

    @Autowired
    UserService service;

    public InlineKeyboardMarkup getMainIKMarkup(Update update) {
        log.trace("Method getMainIKMarkup(Update) started");

        List<InlineKeyboardButton> buttons = new ArrayList<>();
        buttons.add(service.buildIKButton(service.getLocalizationButton(update, APARTMENTS), APARTMENTS));
        buttons.add(service.buildIKButton(service.getLocalizationButton(update, RENT_AN_APARTMENT), RENT_AN_APARTMENT));
        InlineKeyboardRow row1 = service.buildIKRow(buttons);

        buttons.clear();
        buttons.add(service.buildIKButton(service.getLocalizationButton(update, HOUSE_INFORMATION), HOUSE_INFORMATION));
        buttons.add(service.buildIKButton(service.getLocalizationButton(update, CONTACTS), CONTACTS));
        InlineKeyboardRow row2 = service.buildIKRow(buttons);

        buttons.clear();
        buttons.add(service.buildIKButton(service.getLocalizationButton(update, CHANGE_LANGUAGE), CHANGE_LANGUAGE));
        InlineKeyboardRow row3 = service.buildIKRow(buttons);

        log.trace("Method getMainIKMarkup(Update) finished");

        return InlineKeyboardMarkup
                .builder()
                .keyboardRow(row1)
                .keyboardRow(row2)
                .keyboardRow(row3)
                .build();
    }
}
