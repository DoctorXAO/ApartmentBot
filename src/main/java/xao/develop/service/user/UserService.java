package xao.develop.service.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import xao.develop.config.BotConfig;
import xao.develop.config.UserCommand;
import xao.develop.model.TempUserMessage;
import xao.develop.model.UserStatus;
import xao.develop.repository.UserPersistence;

import java.util.List;
import java.util.Locale;

@Slf4j
@Service
public class UserService implements UserCommand {

    @Autowired
    BotConfig botConfig;

    @Autowired
    UserPersistence userPersistence;

    @Autowired
    private MessageSource messageSource;

    public Long getChatId(Update update) {
        log.trace("Method 'getChatId' started");

        Long chatId = update.hasMessage() ?
                update.getMessage().getChatId() :
                update.getCallbackQuery().getMessage().getChatId();

        log.trace("Method 'getChatId' finished and it's returning the next value: {}", chatId);

        return chatId;
    }

    public Integer getMsgId(Update update) {
        log.trace("Method 'getMsgId' started");

        Integer msgId = update.hasMessage() ?
                update.getMessage().getMessageId() :
                update.getCallbackQuery().getMessage().getMessageId();

        log.trace("Method 'getMsgId' finished and it's returning the next value: {}", msgId);

        return msgId;
    }

    private UserStatus getUserLanguage(Update update) {
        log.trace("Method getUserLanguage(Update) started and finished");

        return update.hasMessage() ?
                userPersistence.selectUserStatus(update.getMessage().getChatId()) :
                userPersistence.selectUserStatus(update.getCallbackQuery().getMessage().getChatId());
    }

    private String getLocale(Update update, String code) {
        Locale locale = new Locale(getUserLanguage(update).getLanguage());

        return messageSource.getMessage(code, null, locale);
    }

    public String getLocalizationText(Update update) {
        log.trace("Method getLocalizationText(Update) started");

        String signal = update.hasMessage() ?
                update.getMessage().getText() :
                update.getCallbackQuery().getData();

        try {
            String text;

            switch (signal) {
                case START -> text = getLocale(update, "user.msg.start");
                default -> throw new Exception("Error download message");
            }

            log.debug("Method getLocalizationText(Update) is returning the next value: {}", signal);
            log.trace("Method getLocalizationText(Update) finished");

            return text;
        } catch (Exception ex) {
            log.error("Error loading message. Signal: {}. Error: {}", signal, ex.getMessage());

            return "Error download message. Please, to message the system administrator.";
        }
    }

    public String getLocalizationButton(Update update, String nameButton) {
        log.trace("Method getLocalizationButton(Update, String) started");
        log.debug("getLocalizationButton: nameButton = {}", nameButton);

        String text;

        try {
            switch (nameButton) {
                case APARTMENTS -> text = getLocale(update, "user.bt.apartments");
                case RENT_AN_APARTMENT -> text = getLocale(update, "user.bt.rent-an-apartment");
                case HOUSE_INFORMATION -> text = getLocale(update, "user.bt.house-information");
                case CONTACTS -> text = getLocale(update, "user.bt.contacts");
                case CHANGE_LANGUAGE -> text = getLocale(update, "user.bt.change-language");
                default -> throw new Exception("Ошибка загрузки названия кнопки");
            }
        } catch (Exception ex) {
            log.error("Unknown name button. Name button: {}", nameButton);

            return "null";
        }

        log.trace("Method getLocalizationButton(Update, String) finished");

        return text;
    }

    public void authorization(Message message) {
        Long chatId = message.getChatId();
        String login = message.getFrom().getUserName();
        String firstName = message.getFrom().getFirstName();
        String lastName = message.getFrom().getLastName();
        String language = message.getFrom().getLanguageCode();

        userPersistence.insertUserStatus(chatId, login, firstName, lastName, language, 0);
    }

    public void deleteOldMessages(Update update) {
        log.trace("Method 'deleteOldMessage' started for userID: {}", getChatId(update));

        Long chatId = getChatId(update);

        List<TempUserMessage> userMessages = userPersistence.selectUserMessages(chatId);

        try {
            for(TempUserMessage userMessage : userMessages) {
                DeleteMessage deleteMessage = DeleteMessage
                        .builder()
                        .chatId(userMessage.getChatId())
                        .messageId(userMessage.getMsgId())
                        .build();

                botConfig.getTelegramClient().execute(deleteMessage);
                log.trace("MessageID {} deleted", userMessage.getMsgId());
            }

            userPersistence.deleteUserMessage(chatId);
        } catch (TelegramApiException ex) {
            log.warn("Message to delete not found for userID: {}", chatId);
        }

        log.trace("Method 'deleteOldMessage' finished for userID: {}", chatId);
    }

    public void deleteLastMessages(Update update) {
        registerMessage(getChatId(update), getMsgId(update));
        deleteOldMessages(update);
    }

    public void registerMessage(Long chatId, int messageId) {
        userPersistence.insertUserMessage(chatId, messageId);
    }

    // ------------ Builders ------------

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
        return SendMessage
                .builder()
                .chatId(getChatId(update))
                .text(text)
                .replyMarkup(markup)
                .parseMode("HTML")
                .build();
    }
}
