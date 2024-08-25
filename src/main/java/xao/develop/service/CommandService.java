package xao.develop.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import xao.develop.config.BotConfig;
import xao.develop.model.TempUserMessage;
import xao.develop.model.UserLanguage;
import xao.develop.repository.BotPersistence;

import java.util.List;

public class CommandService implements CommandData {

    @Autowired
    private BotConfig botConfig;

    @Autowired
    private BotPersistence botPersistence;

    public void registerMessage(Long chatId, int messageId) {
        botPersistence.insertUserMessage(chatId, messageId);
    }

    public void deleteOldMessages(Update update) {
        Long chatId;

        if (update.hasMessage())
            chatId = update.getMessage().getChatId();
        else
            chatId = update.getCallbackQuery().getMessage().getChatId();

        List<TempUserMessage> userMessages = botPersistence.selectUserMessages(chatId);

        for(TempUserMessage userMessage : userMessages) {
            try {
                DeleteMessage deleteMessage = DeleteMessage
                        .builder()
                        .chatId(userMessage.getChatId())
                        .messageId(userMessage.getMsgId())
                        .build();

                botConfig.getTelegramClient().execute(deleteMessage);
            } catch (TelegramApiException ex) {
                System.out.println("Message to delete not found"); // todo Добавь логи
            }
        }

        try {
            botPersistence.deleteUserMessage(chatId);
        } catch (InvalidDataAccessApiUsageException ex) {
            System.out.println("No messages to delete");
        }
    }

    public void authorization(Update update) {
        long chatId = update.hasMessage() ?
                update.getMessage().getChatId() :
                update.getCallbackQuery().getMessage().getChatId();
        String language = update.hasMessage() ?
                update.getMessage().getFrom().getLanguageCode() :
                update.getCallbackQuery().getFrom().getLanguageCode();

        botPersistence.insertUserLanguage(chatId, language);
    }

    public void authorization(Update update, String language) {
        long chatId = update.hasMessage() ?
                update.getMessage().getChatId() :
                update.getCallbackQuery().getMessage().getChatId();

        botPersistence.insertUserLanguage(chatId, language);
    }

    private UserLanguage getUserLanguage(Update update) {
        return update.hasMessage() ?
                botPersistence.selectUserLanguage(update.getMessage().getChatId()) :
                botPersistence.selectUserLanguage(update.getCallbackQuery().getMessage().getChatId());
    }

    private Language getLanguage(Update update) {
        Language language;

        switch (getUserLanguage(update).getLanguage()) {
            case TR -> language = new LanguageTR();
            case RU -> language = new LanguageRU();
            default -> language = new LanguageEN();
        }

        return language;
    }

    public String getLocalizationText(Update update) {
        try {
            String text;

            Language language = getLanguage(update);

            if (update.hasMessage()) {
                switch (update.getMessage().getText()) {
                    case START -> text = language.getStart();
                    default -> throw new Exception("Ошибка загрузки сообщения");
                }
            } else {
                switch (update.getCallbackQuery().getData()) {
                    case APARTMENTS -> text = language.getApartments();
                    case RENT_AN_APARTMENT -> text = language.getRentAnApartment();
                    case HOUSE_INFORMATION,
                         BACK_TO_HOUSE_INFORMATION -> text = language.getHouseInformation();
                    case RULES -> text = language.getRules();
                    case CONTACTS -> text = language.getContacts(botConfig.getPhone(), botConfig.getEmail());
                    case CHANGE_LANGUAGE -> text = language.getChangeLanguage();
                    case BACK_TO_START,
                         TR,
                         EN,
                         RU -> text = language.getStart();
                    default -> throw new Exception("Ошибка загрузки сообщения");
                }
            }

            return text;
        } catch (Exception ex) {
            ex.printStackTrace(); // todo Сделай логи

            return "Ошибка загрузки сообщения. Пожалуйста, сообщите системному администратору.";
        }
    }

    public String getLocalizationButton(Update update, String nameButton) {
        String text;

        Language language = getLanguage(update);

        try {
            switch (nameButton) {
                case APARTMENTS -> text = language.getButtonApartments();
                case RENT_AN_APARTMENT -> text = language.getButtonRentAnApartment();
                case HOUSE_INFORMATION -> text = language.getButtonHouseInformation();
                case RULES -> text = language.getButtonRules();
                case CONTACTS -> text = language.getButtonContacts();
                case CHANGE_LANGUAGE -> text = language.getButtonChangeLanguage();
                case "back" -> text = language.getButtonBack();
                default -> throw new Exception("Ошибка загрузки названия кнопки");
            }
        } catch (Exception ex) {
            ex.printStackTrace();

            return "null";
        }

        return text;
    }
}
