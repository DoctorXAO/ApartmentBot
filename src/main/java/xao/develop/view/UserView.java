package xao.develop.view;

import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import xao.develop.config.BotConfig;
import xao.develop.service.CommandData;
import xao.develop.service.CommandService;

import java.util.ArrayList;
import java.util.List;

public class UserView implements Account, User, CommandData {

    @Autowired
    private BotConfig botConfig;

    @Autowired
    private CommandService commandService;

    @Override
    public void core(Update update) {
        if ((update.hasMessage() && update.getMessage().hasText()) || update.hasCallbackQuery())
            commandService.deleteOldMessages(update);

        Message message = new Message();

        if (update.hasMessage() && update.getMessage().hasText()) {
            String msg = update.getMessage().getText();

            try {
                commandService.registerMessage(update.getMessage().getChatId(), update.getMessage().getMessageId());
                commandService.deleteOldMessages(update);

                if (msg.equals(START)) {
                    message = cmd_start(update);
                    commandService.registerMessage(message.getChatId(), message.getMessageId());
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
                    case HOUSE_INFORMATION,
                         BACK_TO_HOUSE_INFORMATION -> message = data_house_information(update);
                    case CONTACTS -> message = data_contacts(update);
                    case RULES -> message = data_rules(update);
                    case CHANGE_LANGUAGE -> message = data_change_language(update);
                    case TR,
                         EN,
                         RU -> message = changeLanguage(update, data);
                    case BACK_TO_START -> message = data_start(update);
                    default -> dataIsNotRecognized();
                }

                commandService.registerMessage(message.getChatId(), message.getMessageId());
            } catch (TelegramApiException ex) {
                ex.printStackTrace(); // todo Добавь логи
            }
        }
    }

    private Message changeLanguage(Update update, String language) throws TelegramApiException {
        commandService.authorization(update, language);
        return data_start(update);
    }

    @Override
    public Message cmd_start(Update update) throws TelegramApiException {
        commandService.authorization(update);

        return botConfig.getTelegramClient().execute(buildSendMessage(update,
                commandService.getLocalizationText(update),
                getMainIKMarkup(update)));
    }

    @Override
    public Message data_start(Update update) throws TelegramApiException {
        return botConfig.getTelegramClient().execute(buildSendMessage(update,
                commandService.getLocalizationText(update),
                getMainIKMarkup(update)));
    }

    @Override
    public Message data_apartments(Update update) throws TelegramApiException {
        return botConfig.getTelegramClient().execute(buildSendMessage(update,
                commandService.getLocalizationText(update),
                getApartmentsIKMarkup(update)));
    }

    @Override
    public Message data_rent_an_apartment(Update update) throws TelegramApiException {
        return botConfig.getTelegramClient().execute(buildSendMessage(update,
                commandService.getLocalizationText(update),
                getRentAnApartmentIKMarkup(update)));
    }

    @Override
    public Message data_house_information(Update update) throws TelegramApiException {
        return botConfig.getTelegramClient().execute(buildSendMessage(update,
                commandService.getLocalizationText(update),
                getHouseInformationIKMarkup(update)));
    }

    @Override
    public Message data_contacts(Update update) throws TelegramApiException {
        return botConfig.getTelegramClient().execute(buildSendMessage(update,
                commandService.getLocalizationText(update),
                InlineKeyboardMarkup
                        .builder()
                        .keyboardRow(new InlineKeyboardRow(buildIKButton(
                                commandService.getLocalizationButton(update, "back"), BACK_TO_START)))
                        .build()));
    }

    @Override
    public Message data_rules(Update update) throws TelegramApiException {
        return botConfig.getTelegramClient().execute(buildSendMessage(update,
                commandService.getLocalizationText(update),
                InlineKeyboardMarkup
                        .builder()
                        .keyboardRow(new InlineKeyboardRow(buildIKButton(
                                commandService.getLocalizationButton(update, "back"), BACK_TO_HOUSE_INFORMATION)))
                        .build()));
    }

    @Override
    public Message data_change_language(Update update) throws TelegramApiException {
        return botConfig.getTelegramClient().execute(buildSendMessage(update,
                commandService.getLocalizationText(update),
                InlineKeyboardMarkup
                        .builder()
                        .keyboardRow(new InlineKeyboardRow(buildIKButton(
                                commandService.getLocalizationButton(update, "back"), BACK_TO_START)))
                        .keyboardRow(new InlineKeyboardRow(buildIKButton("\uD83C\uDDF9\uD83C\uDDF7 Türkçe", TR)))
                        .keyboardRow(new InlineKeyboardRow(buildIKButton("\uD83C\uDDEC\uD83C\uDDE7 English", EN)))
                        .keyboardRow(new InlineKeyboardRow(buildIKButton("\uD83C\uDDF7\uD83C\uDDFA Русский", RU)))
                        .build()));
    }

    @Override
    public Message dataIsNotRecognized() {
        return null;
    }

    private InlineKeyboardMarkup getMainIKMarkup(Update update) {
        List<InlineKeyboardButton> buttons = new ArrayList<>();
        buttons.add(buildIKButton(commandService.getLocalizationButton(update, APARTMENTS), APARTMENTS));
        buttons.add(buildIKButton(commandService.getLocalizationButton(update, RENT_AN_APARTMENT), RENT_AN_APARTMENT));
        InlineKeyboardRow row1 = buildIKRow(buttons);

        buttons.clear();
        buttons.add(buildIKButton(commandService.getLocalizationButton(update, HOUSE_INFORMATION), HOUSE_INFORMATION));
        buttons.add(buildIKButton(commandService.getLocalizationButton(update, CONTACTS), CONTACTS));
        InlineKeyboardRow row2 = buildIKRow(buttons);

        buttons.clear();
        buttons.add(buildIKButton(commandService.getLocalizationButton(update, CHANGE_LANGUAGE), CHANGE_LANGUAGE));
        InlineKeyboardRow row3 = buildIKRow(buttons);

        return InlineKeyboardMarkup
                .builder()
                .keyboardRow(row1)
                .keyboardRow(row2)
                .keyboardRow(row3)
                .build();
    }

    private InlineKeyboardMarkup getHouseInformationIKMarkup(Update update) {
        return InlineKeyboardMarkup
                .builder()
                .keyboardRow(new InlineKeyboardRow(buildIKButton(
                        commandService.getLocalizationButton(update, RULES), RULES)))
                .keyboardRow(new InlineKeyboardRow(buildIKButton(
                        commandService.getLocalizationButton(update, "back"), BACK_TO_START)))
                .build();
    }

    private InlineKeyboardMarkup getApartmentsIKMarkup(Update update) {
        return InlineKeyboardMarkup
                .builder()
                .keyboardRow(new InlineKeyboardRow(buildIKButton(
                        commandService.getLocalizationButton(update, "back"), BACK_TO_START)))
                .build();
    }

    private InlineKeyboardMarkup getRentAnApartmentIKMarkup(Update update) {
        return InlineKeyboardMarkup
                .builder()
                .keyboardRow(new InlineKeyboardRow(buildIKButton("\uD83D\uDD8A Заполнить заявку", "fill_out_an_application")))
                .keyboardRow(new InlineKeyboardRow(buildIKButton(
                        commandService.getLocalizationButton(update, "back"), BACK_TO_START)))
                .build();
    }

    private InlineKeyboardRow buildIKRow(List<InlineKeyboardButton> buttons) {
        return new InlineKeyboardRow(buttons);
    }

    private InlineKeyboardButton buildIKButton(String text, String callbackData) {
        return InlineKeyboardButton
                .builder()
                .text(text)
                .callbackData(callbackData)
                .build();
    }

    private SendMessage buildSendMessage(Update update, String text, InlineKeyboardMarkup markup) {
        long chatID;
        if (update.hasMessage())
            chatID = update.getMessage().getChatId();
        else if (update.hasCallbackQuery())
            chatID = update.getCallbackQuery().getMessage().getChatId();
        else
            chatID = 0;

        return SendMessage
                .builder()
                .chatId(chatID)
                .text(text)
                .replyMarkup(markup)
                .parseMode("HTML")
                .build();
    }
}
