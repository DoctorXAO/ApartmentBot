package xao.develop.view.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import xao.develop.config.BotConfig;
import xao.develop.service.UserData;
import xao.develop.service.UserService;
import xao.develop.view.Account;

@Component
public class UserView implements Account, User, UserData {

    @Autowired
    private BotConfig botConfig;

    @Autowired
    private UserService userService;

    @Override
    public void core(Update update) {
        if ((update.hasMessage() && update.getMessage().hasText()) || update.hasCallbackQuery())
            userService.deleteOldMessages(update);

        Message message = new Message();

        if (update.hasMessage() && update.getMessage().hasText()) {
            String msg = update.getMessage().getText();

            try {
                userService.registerMessage(update.getMessage().getChatId(), update.getMessage().getMessageId());
                userService.deleteOldMessages(update);

                if (msg.equals(START)) {
                    message = cmd_start(update);
                    userService.registerMessage(message.getChatId(), message.getMessageId());
                }
            } catch (TelegramApiException ex) {
                ex.printStackTrace(); // todo Добавь логи
            }
        } else if (update.hasCallbackQuery()) {
            String data = update.getCallbackQuery().getData();

            try {
                switch (data) {
                    case APARTMENTS -> message = data_apartments(update);
                    case RENT_AN_APARTMENT -> message = data_rent_an_apartment(update);
                    case HOUSE_INFORMATION -> message = data_house_information(update);
                    case CONTACTS -> message = data_contacts(update);
                    case RULES -> message = data_rules(update);
                    case CHANGE_LANGUAGE -> message = data_change_language(update);
                    case TR,
                         EN,
                         RU -> message = changeLanguage(update, data);
                    case BACK_TO_START -> message = data_start(update);
                }

                userService.registerMessage(message.getChatId(), message.getMessageId());
            } catch (TelegramApiException ex) {
                ex.printStackTrace(); // todo Добавь логи
            }
        }
    }

    private Message changeLanguage(Update update, String language) throws TelegramApiException {
        userService.authorization(update, language);
        return data_start(update);
    }

    @Override
    public Message cmd_start(Update update) throws TelegramApiException {
        userService.authorization(update);

        return botConfig.getTelegramClient().execute(userService.buildSendMessage(update,
                userService.getLocalizationText(update),
                userService.getMainIKMarkup(update)));
    }

    @Override
    public Message data_start(Update update) throws TelegramApiException {
        return botConfig.getTelegramClient().execute(userService.buildSendMessage(update,
                userService.getLocalizationText(update),
                userService.getMainIKMarkup(update)));
    }

    @Override
    public Message data_apartments(Update update) throws TelegramApiException {
        return botConfig.getTelegramClient().execute(userService.buildSendMessage(update,
                userService.getLocalizationText(update),
                userService.getBackIKMarkup(update, BACK_TO_START)));
    }

    @Override
    public Message data_rent_an_apartment(Update update) throws TelegramApiException {
        return botConfig.getTelegramClient().execute(userService.buildSendMessage(update,
                userService.getLocalizationText(update),
                userService.getRentAnApartmentIKMarkup(update)));
    }

    @Override
    public Message data_fill_out_an_application(Update update) throws TelegramApiException {
        return botConfig.getTelegramClient().execute(userService.buildSendMessage(update,
                userService.getLocalizationText(update),
                userService.getBackIKMarkup(update, RENT_AN_APARTMENT)));
    }

    @Override
    public Message data_house_information(Update update) throws TelegramApiException {
        return botConfig.getTelegramClient().execute(userService.buildSendMessage(update,
                userService.getLocalizationText(update),
                userService.getHouseInformationIKMarkup(update)));
    }

    @Override
    public Message data_contacts(Update update) throws TelegramApiException {
        return botConfig.getTelegramClient().execute(userService.buildSendMessage(update,
                userService.getLocalizationText(update),
                InlineKeyboardMarkup
                        .builder()
                        .keyboardRow(new InlineKeyboardRow(userService.buildIKButton(
                                userService.getLocalizationButton(update, BACK), BACK_TO_START)))
                        .build()));
    }

    @Override
    public Message data_rules(Update update) throws TelegramApiException {
        return botConfig.getTelegramClient().execute(userService.buildSendMessage(update,
                userService.getLocalizationText(update),
                InlineKeyboardMarkup
                        .builder()
                        .keyboardRow(new InlineKeyboardRow(userService.buildIKButton(
                                userService.getLocalizationButton(update, BACK), HOUSE_INFORMATION)))
                        .build()));
    }

    @Override
    public Message data_change_language(Update update) throws TelegramApiException {
        return botConfig.getTelegramClient().execute(userService.buildSendMessage(update,
                userService.getLocalizationText(update),
                InlineKeyboardMarkup
                        .builder()
                        .keyboardRow(new InlineKeyboardRow(userService.buildIKButton(
                                userService.getLocalizationButton(update, BACK), BACK_TO_START)))
                        .keyboardRow(new InlineKeyboardRow(
                                userService.buildIKButton("\uD83C\uDDF9\uD83C\uDDF7 Türkçe", TR)))
                        .keyboardRow(new InlineKeyboardRow(
                                userService.buildIKButton("\uD83C\uDDEC\uD83C\uDDE7 English", EN)))
                        .keyboardRow(new InlineKeyboardRow(
                                userService.buildIKButton("\uD83C\uDDF7\uD83C\uDDFA Русский", RU)))
                        .build()));
    }
}
