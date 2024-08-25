package xao.develop.view.User;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import xao.develop.config.BotConfig;
import xao.develop.service.UserData;
import xao.develop.service.UserService;
import xao.develop.view.Account;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

        if (update.hasMessage() && update.getMessage().hasText())
            processingTheMessage(update);
        else if (update.hasCallbackQuery())
            processingTheCallbackQuery(update);
    }

    // Обработка входящего сообщений
    private void processingTheMessage(Update update) {
        String msg = update.getMessage().getText();

        try {
            userService.registerMessage(update.getMessage().getChatId(), update.getMessage().getMessageId());
            userService.deleteOldMessages(update);

            if (msg.equals(START)) {
                Message message = cmd_start(update);
                userService.registerMessage(message.getChatId(), message.getMessageId());
            }
        } catch (TelegramApiException ex) {
            ex.printStackTrace(); // todo Добавь логи
        }
    }

    // Обработка входящего сигнала
    private void processingTheCallbackQuery(Update update) {
        String data = update.getCallbackQuery().getData();
        List<Message> messages;

        try {
            switch (data) {
                case APARTMENTS -> messages = data_apartments(update);
                case RENT_AN_APARTMENT -> messages= data_rent_an_apartment(update);
                case HOUSE_INFORMATION -> messages = data_house_information(update);
                case CONTACTS -> messages = data_contacts(update);
                case RULES -> messages = data_rules(update);
                case CHANGE_LANGUAGE -> messages = data_change_language(update);
                case TR,
                     EN,
                     RU -> messages = changeLanguage(update, data);
                default -> messages = data_start(update);
            }

            for (Message message : messages) {
                if (message == null)
                    continue;

                userService.registerMessage(message.getChatId(), message.getMessageId());
            }
        } catch (TelegramApiException ex) {
            ex.printStackTrace(); // todo Добавь логи
        }
    }

    // Смена языка интерфейса пользователя
    private List<Message> changeLanguage(Update update, String language) throws TelegramApiException {
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
    public List<Message> data_start(Update update) throws TelegramApiException {
        return Collections.singletonList(
                botConfig.getTelegramClient().execute(userService.buildSendMessage(update,
                userService.getLocalizationText(update),
                userService.getMainIKMarkup(update))));
    }

    @Override
    public @NotNull List<Message> data_apartments(Update update) throws TelegramApiException {
        List<Message> messages = new ArrayList<>(
                botConfig.getTelegramClient().execute(
                userService.sendPhotos(update, "apartment")));

        messages.add(botConfig.getTelegramClient().execute(userService.buildSendMessage(update,
                userService.getLocalizationText(update),
                userService.getBackIKMarkup(update, BACK_TO_START))));

        return messages;
    }

    @Override
    public List<Message> data_rent_an_apartment(Update update) throws TelegramApiException {
        return Collections.singletonList(
                botConfig.getTelegramClient().execute(userService.buildSendMessage(update,
                userService.getLocalizationText(update),
                userService.getRentAnApartmentIKMarkup(update))));
    }

    @Override
    public List<Message> data_fill_out_an_application(Update update) throws TelegramApiException {
        return Collections.singletonList(
                botConfig.getTelegramClient().execute(userService.buildSendMessage(update,
                userService.getLocalizationText(update),
                userService.getBackIKMarkup(update, RENT_AN_APARTMENT))));
    }

    @Override
    public List<Message> data_house_information(Update update) throws TelegramApiException {
        return Collections.singletonList(
                botConfig.getTelegramClient().execute(userService.buildSendMessage(update,
                userService.getLocalizationText(update),
                userService.getHouseInformationIKMarkup(update))));
    }

    @Override
    public List<Message> data_contacts(Update update) throws TelegramApiException {
        return Collections.singletonList(
                botConfig.getTelegramClient().execute(userService.buildSendMessage(update,
                userService.getLocalizationText(update),
                userService.getBackIKMarkup(update, BACK_TO_START))));
    }

    @Override
    public List<Message> data_rules(Update update) throws TelegramApiException {
        return Collections.singletonList(
                botConfig.getTelegramClient().execute(userService.buildSendMessage(update,
                userService.getLocalizationText(update),
                userService.getBackIKMarkup(update, HOUSE_INFORMATION))));
    }

    @Override
    public List<Message> data_change_language(Update update) throws TelegramApiException {
        return Collections.singletonList(
                botConfig.getTelegramClient().execute(userService.buildSendMessage(update,
                userService.getLocalizationText(update),
                userService.getChangeLanguageIKMarkup(update))));
    }
}
