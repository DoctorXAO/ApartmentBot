package xao.develop.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import xao.develop.config.BotConfig;
import xao.develop.model.AccountStatus;
import xao.develop.model.TempBotMessage;
import xao.develop.repository.Persistence;

import java.util.List;
import java.util.Locale;
import java.util.MissingFormatArgumentException;

@Slf4j
@Service
public class BotService {

    @Autowired
    BotConfig botConfig;

    @Autowired
    Persistence persistence;

    @Autowired
    MessageSource messageSource;

    public void setLanguage(Update update, String language) {
        persistence.updateLanguageInAccountStatus(getChatId(update), language);
    }

    public Object[] getAdminContacts() {
        return new Object[]{botConfig.getPhone(), botConfig.getEmail()};
    }

    public Long getChatId(Update update) {
        log.trace("Method 'getChatId' started");

        Long chatId = update.hasMessage() ?
                update.getMessage().getChatId() :
                update.getCallbackQuery().getMessage().getChatId();

        log.trace("Method 'getChatId' finished and it's returning the next value: {}", chatId);

        return chatId;
    }

    public Integer getMessageId(Update update) {
        log.trace("Method getMessageId(Update) started");

        Integer messageId = update.hasMessage() ?
                update.getMessage().getMessageId() :
                update.getCallbackQuery().getMessage().getMessageId();

        log.trace("Method getMessageId(Update) finished and it's returning the next value: {}", messageId);

        return messageId;
    }

    public User getUser(Update update) {
        return update.hasMessage() ?
                update.getMessage().getFrom() :
                update.getCallbackQuery().getFrom();
    }

    public String getData(Update update) {
        log.trace("Method getData(Update) started and finished");

        if (update.hasMessage())
            return update.getMessage().getText();
        else
            return update.getCallbackQuery().getData();
    }

    public String getLocaleMessage(Update update, String msgLink, Object... args) {
        AccountStatus accountStatus = update.hasMessage() ?
                persistence.selectAccountStatus(update.getMessage().getChatId()) :
                persistence.selectAccountStatus(update.getCallbackQuery().getMessage().getChatId());

        Locale locale = new Locale(accountStatus.getLanguage());

        log.debug("Method getLocaleMessage(Update, String) get the next value: {}", msgLink);

        try {
            return String.format(messageSource.getMessage(msgLink, null, locale), args);
        } catch (MissingFormatArgumentException | IndexOutOfBoundsException ex) {
            log.warn("""
                    Impossible to format message link: {}
                    Args: {}
                    Exception: {}""", msgLink, args, ex.getMessage());
            return messageSource.getMessage(msgLink, null, locale);
        }
    }

    public void authorization(Message message) {
        log.trace("Method authorization(Message) started");

        Long chatId = message.getChatId();
        String language = message.getFrom().getLanguageCode();

        persistence.insertAccountStatus(chatId, language);
        persistence.deleteTempBookingData(chatId);

        log.debug("""
                Authorization: the following user parameters have been added to the database:
                chatId = {}
                language = {}
                """, chatId, language);

        log.trace("Method authorization(Message) finished");
    }

    public void deleteOldMessages(Update update) {
        log.trace("Method 'deleteOldMessage' started for userID: {}", getChatId(update));

        Long chatId = getChatId(update);

        List<TempBotMessage> userMessages = persistence.selectTempBotMessages(chatId);

        for(TempBotMessage userMessage : userMessages) {
            try {
                log.debug("Try to delete the next message: {}", userMessage.getMsgId());
                DeleteMessage deleteMessage = DeleteMessage
                        .builder()
                        .chatId(userMessage.getChatId())
                        .messageId(userMessage.getMsgId())
                        .build();

                botConfig.getTelegramClient().execute(deleteMessage);
                log.debug("MessageID {} deleted", userMessage.getMsgId());
            } catch (TelegramApiException ex) {
                log.warn("""
                    Message to delete not found for userID: {}
                    Exception: {}""", chatId, ex.getMessage());
            }
        }

        persistence.deleteTempBotMessages(chatId);

        log.trace("Method 'deleteOldMessage' finished for userID: {}", chatId);
    }

    public void deleteLastMessage(long chatId, int msgId) {
        log.trace("Method deleteLastMessage(Update) started for userID: {}", chatId);

        try {
            botConfig.getTelegramClient().execute(DeleteMessage
                    .builder()
                    .chatId(chatId)
                    .messageId(msgId)
                    .build());

            log.debug("Last messageID {} deleted", msgId);
        } catch (TelegramApiException ex) {
            log.warn("""
                    Can't delete messageId {} with userID {}
                    Exception: {}""",
                    msgId, chatId, ex.getMessage());
        }

        log.trace("Method deleteLastMessage(Update) finished for userID: {}", chatId);
    }

    public void deleteMessage(long chatId, int msgId) {
        try {
            DeleteMessage deleteMessage = DeleteMessage
                    .builder()
                    .chatId(chatId)
                    .messageId(msgId)
                    .build();

            botConfig.getTelegramClient().execute(deleteMessage);

            persistence.deleteMessageTempBotMessage(chatId, msgId);

            log.debug("The next MessageID {} deleted", msgId);
        } catch (TelegramApiException ex) {
            log.warn("""
                                    Impossible to delete message {} for user {}
                                    Exception: {}""",
                    msgId, chatId, ex.getMessage());
        }
    }

    public int deleteAllMessagesExceptTheLastOne(Update update) {
        List<TempBotMessage> tempBotMessages = persistence.selectTempBotMessages(getChatId(update));

        for (int i = 0; i < tempBotMessages.size() - 1; i++) {
            TempBotMessage tempBotMessage = tempBotMessages.get(i);
            deleteMessage(getChatId(update), tempBotMessage.getMsgId());
        }

        return tempBotMessages.get(tempBotMessages.size() - 1).getMsgId();
    }

    public void registerMessage(long chatId, int messageId) {
        persistence.insertTempBotMessage(chatId, messageId);
        log.debug("The message {} registered", messageId);
    }

    public int sendSimpleMessage(Update update, String msgLink) throws TelegramApiException {
            return botConfig.getTelegramClient().execute(SendMessage
                    .builder()
                    .chatId(getChatId(update))
                    .text(getLocaleMessage(update, msgLink))
                    .parseMode("HTML")
                    .build()).getMessageId();
    }

    public SendMessage sendMessage(Update update, String text, InlineKeyboardMarkup markup) {
        return SendMessage
                .builder()
                .chatId(getChatId(update))
                .text(text)
                .replyMarkup(markup)
                .parseMode("HTML")
                .build();
    }

    public EditMessageText editMessageText(Update update, int msgId, String msg, InlineKeyboardMarkup markup) {
        return EditMessageText
                .builder()
                .chatId(getChatId(update))
                .messageId(msgId)
                .text(msg)
                .replyMarkup(markup)
                .parseMode("HTML")
                .build();
    }

    public void sendMessageAdminUser(long chatId, Keyboard keyboard) {

    }

    public SendMessage sendMessageAdminUser(String msgLink, Keyboard keyboard, Object... args) {
        InlineKeyboardMarkup markup;

        switch (keyboard) {
            case CONFIRM_BOOKING -> markup = null;
            case ANSWER -> markup = null;
            default -> markup = null;
        }

        return SendMessage
                .builder()
                .chatId(botConfig.getAdminId())
                .text(msgLink)
                .replyMarkup(markup)
                .parseMode("HTML")
                .build();
    }
}

