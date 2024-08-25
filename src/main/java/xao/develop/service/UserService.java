package xao.develop.service;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import xao.develop.config.BotConfig;
import xao.develop.model.TempUserMessage;
import xao.develop.model.UserLanguage;
import xao.develop.repository.BotPersistence;
import xao.develop.service.Languages.Language;
import xao.develop.service.Languages.LanguageEN;
import xao.develop.service.Languages.LanguageRU;
import xao.develop.service.Languages.LanguageTR;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.util.*;

@Service
public class UserService implements UserData {

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

    public SendMediaGroup sendPhotos(Update update, String patch) {
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            URL resource = classLoader.getResource(patch);

            if (resource == null)
                throw new Exception("Директория с фото не найдена!");

            List<InputMediaPhoto> photos = new ArrayList<>();

            File[] files = getSortedFiles(resource);

            for (File file : files)
                photos.add(new InputMediaPhoto(file, file.getName()));

            if (photos.size() == 1)
                throw new Exception("В процессе отправка одного фото...");
            else if (photos.size() < 2)
                throw new Exception("В указанной директории меньше, чем два фото расширения *.jpg/*.png");

            return SendMediaGroup
                    .builder()
                    .chatId(update.getCallbackQuery().getMessage().getChatId())
                    .medias(photos)
                    .build();
        } catch (Exception ex) {
            ex.printStackTrace(); // todo Сделать логирование

            return null;
        }
    }

    private @NotNull File [] getSortedFiles(URL resource) throws Exception {
        File directory = new File(resource.getFile());

        FilenameFilter filter = (dir, name) ->
                name.endsWith(".jpg") ||
                name.endsWith(".jpeg") ||
                name.endsWith(".png");

        File[] files = directory.listFiles(filter);

        if (!directory.isDirectory())
            throw new Exception("Указанный путь не является директорией!");
        else if (files == null)
            throw new Exception("В директории нет изображений с необходимым расширением (.jpg/.jpeg./.png)!");

        Arrays.sort(files, Comparator.comparing(File::getName));

        return files;
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
                    case FILL_OUT_AN_APPLICATION -> text = null;
                    case HOUSE_INFORMATION -> text = language.getHouseInformation();
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
                case FILL_OUT_AN_APPLICATION -> text = language.getButtonFillOutAnApplication();
                case HOUSE_INFORMATION -> text = language.getButtonHouseInformation();
                case RULES -> text = language.getButtonRules();
                case CONTACTS -> text = language.getButtonContacts();
                case CHANGE_LANGUAGE -> text = language.getButtonChangeLanguage();
                case BACK -> text = language.getButtonBack();
                default -> throw new Exception("Ошибка загрузки названия кнопки");
            }
        } catch (Exception ex) {
            ex.printStackTrace();

            return "null";
        }

        return text;
    }

    public InlineKeyboardMarkup getMainIKMarkup(Update update) {
        List<InlineKeyboardButton> buttons = new ArrayList<>();
        buttons.add(buildIKButton(getLocalizationButton(update, APARTMENTS), APARTMENTS));
        buttons.add(buildIKButton(getLocalizationButton(update, RENT_AN_APARTMENT), RENT_AN_APARTMENT));
        InlineKeyboardRow row1 = buildIKRow(buttons);

        buttons.clear();
        buttons.add(buildIKButton(getLocalizationButton(update, HOUSE_INFORMATION), HOUSE_INFORMATION));
        buttons.add(buildIKButton(getLocalizationButton(update, CONTACTS), CONTACTS));
        InlineKeyboardRow row2 = buildIKRow(buttons);

        buttons.clear();
        buttons.add(buildIKButton(getLocalizationButton(update, CHANGE_LANGUAGE), CHANGE_LANGUAGE));
        InlineKeyboardRow row3 = buildIKRow(buttons);

        return InlineKeyboardMarkup
                .builder()
                .keyboardRow(row1)
                .keyboardRow(row2)
                .keyboardRow(row3)
                .build();
    }

    public InlineKeyboardMarkup getHouseInformationIKMarkup(Update update) {
        return InlineKeyboardMarkup
                .builder()
                .keyboardRow(new InlineKeyboardRow(buildIKButton(
                        getLocalizationButton(update, RULES), RULES)))
                .keyboardRow(new InlineKeyboardRow(buildIKButton(
                        getLocalizationButton(update, "back"), BACK_TO_START)))
                .build();
    }

    public InlineKeyboardMarkup getRentAnApartmentIKMarkup(Update update) {
        return InlineKeyboardMarkup
                .builder()
                .keyboardRow(new InlineKeyboardRow(buildIKButton(
                        getLocalizationButton(update, "fill_out_an_application"), FILL_OUT_AN_APPLICATION)))
                .keyboardRow(new InlineKeyboardRow(buildIKButton(
                        getLocalizationButton(update, "back"), BACK_TO_START)))
                .build();
    }

    public InlineKeyboardMarkup getChangeLanguageIKMarkup(Update update) {
        return InlineKeyboardMarkup
                .builder()
                .keyboardRow(new InlineKeyboardRow(buildIKButton(
                        getLocalizationButton(update, BACK), BACK_TO_START)))
                .keyboardRow(new InlineKeyboardRow(
                        buildIKButton("\uD83C\uDDF9\uD83C\uDDF7 Türkçe", TR)))
                .keyboardRow(new InlineKeyboardRow(
                        buildIKButton("\uD83C\uDDEC\uD83C\uDDE7 English", EN)))
                .keyboardRow(new InlineKeyboardRow(
                        buildIKButton("\uD83C\uDDF7\uD83C\uDDFA Русский", RU)))
                .build();
    }

    public InlineKeyboardMarkup getBackIKMarkup(Update update, String direction) {
        return InlineKeyboardMarkup
                .builder()
                .keyboardRow(new InlineKeyboardRow(buildIKButton(
                        getLocalizationButton(update, "back"), direction)))
                .build();
    }

    public InlineKeyboardRow buildIKRow(List<InlineKeyboardButton> buttons) {
        return new InlineKeyboardRow(buttons);
    }

    public InlineKeyboardButton buildIKButton(String text, String callbackData) {
        return InlineKeyboardButton
                .builder()
                .text(text)
                .callbackData(callbackData)
                .build();
    }

    public SendMessage buildSendMessage(Update update, String text, InlineKeyboardMarkup markup) {
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
