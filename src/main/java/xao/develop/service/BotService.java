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
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import xao.develop.config.BotConfig;
import xao.develop.model.AccountStatus;
import xao.develop.model.TempBotMessage;
import xao.develop.repository.Persistence;

import java.net.URL;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class BotService {

    @Autowired
    BotConfig botConfig;

    @Autowired
    Persistence persistence;

    @Autowired
    MessageSource messageSource;

    private final ScheduledExecutorService scheduled = Executors.newScheduledThreadPool(1);

    public final URL resourceApartments = getClass().getClassLoader().getResource("img/apartments/");
    public final URL resourceLanguages = getClass().getClassLoader().getResource("languages/");

    // setters

    public void setLanguage(long chatId, String language) {
        persistence.updateLanguageInAccountStatus(chatId, language);
    }

    // getters

    public long getAdminId() {
        return botConfig.getAdminId();
    }

    public Object[] getAdminContacts() {
        return new Object[]{botConfig.getPhone(), botConfig.getEmail()};
    }

    public long getChatId(Update update) {
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

    public String getLocaleMessage(long chatId, String msgLink, Object... args) {
        AccountStatus accountStatus = persistence.selectAccountStatus(chatId);

        Locale locale = new Locale(accountStatus.getLanguage());

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

    // actions

    public void authorization(long chatId, User user) {
        log.trace("Method authorization(Message) started");

        persistence.insertAccountStatus(chatId, user.getLanguageCode());
        persistence.deleteTempBookingData(chatId);

        log.debug("""
                Authorization: the following user parameters have been added to the database:
                chatId = {}
                language = {}
                """, chatId, user.getLanguageCode());

        log.trace("Method authorization(Message) finished");
    }

    public void deleteOldMessages(long chatId) {
        log.trace("Method 'deleteOldMessage' started for userID: {}", chatId);

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

    public void deleteMessage(long chatId, int msgId) {
        try {
            botConfig.getTelegramClient().execute(DeleteMessage
                    .builder()
                    .chatId(chatId)
                    .messageId(msgId)
                    .build());

            persistence.deleteMessageTempBotMessage(chatId, msgId);

            log.debug("The next MessageID {} deleted", msgId);
        } catch (TelegramApiException ex) {
            log.warn("""
                                    Impossible to delete message {} for user {}
                                    Exception: {}""",
                    msgId, chatId, ex.getMessage());
        }
    }

    public int deleteAllMessagesExceptTheLastOne(long chatId) {
        List<TempBotMessage> tempBotMessages = persistence.selectTempBotMessages(chatId);

        for (int i = 0; i < tempBotMessages.size() - 1; i++) {
            TempBotMessage tempBotMessage = tempBotMessages.get(i);
            deleteMessage(chatId, tempBotMessage.getMsgId());
        }

        return tempBotMessages.get(tempBotMessages.size() - 1).getMsgId();
    }

    public void registerMessage(long chatId, int messageId) {
        if (messageId != 0) {
            persistence.insertTempBotMessage(chatId, messageId);

            log.debug("The message {} registered", messageId);
        }
    }

    public int sendSimpleMessage(long chatId, String msgLink, Object... args) throws TelegramApiException {
            return botConfig.getTelegramClient().execute(SendMessage
                    .builder()
                    .chatId(chatId)
                    .text(getLocaleMessage(chatId, msgLink, args))
                    .parseMode("HTML")
                    .build()).getMessageId();
    }

    public SendMessage sendMessage(long chatId, String text, InlineKeyboardMarkup markup) {
        return SendMessage
                .builder()
                .chatId(chatId)
                .text(text)
                .replyMarkup(markup)
                .parseMode("HTML")
                .build();
    }

    public void sendMessageInfo(long chatId,
                                String msgLink,
                                InlineKeyboardMarkup markup,
                                Object... args) throws TelegramApiException {

        botConfig.getTelegramClient().execute(sendMessage(
                chatId,
                getLocaleMessage(chatId, msgLink, args),
                markup));
    }
    
    public void sendTempMessage(long chatId, String msgLink, int forTime) throws TelegramApiException {
        int msgId = sendSimpleMessage(chatId, msgLink, forTime);
        scheduled.schedule(() -> deleteMessage(chatId, msgId), forTime, TimeUnit.SECONDS);
    }
    
    public void lateDeleteMessage(long chatId, int msgId, int forTime) {
        scheduled.schedule(() -> deleteMessage(chatId, msgId), forTime, TimeUnit.SECONDS);
    }

    public EditMessageText editMessageText(long chatId, int msgId, String msg, InlineKeyboardMarkup markup) {
        return EditMessageText
                .builder()
                .chatId(chatId)
                .messageId(msgId)
                .text(msg)
                .replyMarkup(markup)
                .parseMode("HTML")
                .build();
    }

    public String getCheckDate(Long checkTimeInMillis) {
        Calendar calendar = persistence.getServerPresentTime();

        calendar.setTimeInMillis(checkTimeInMillis);
        String day = calendar.get(Calendar.DAY_OF_MONTH) < 10 ?
                "0" + calendar.get(Calendar.DAY_OF_MONTH) : String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
        String month = calendar.get(Calendar.MONTH) + 1 < 10 ?
                "0" + (calendar.get(Calendar.MONTH) + 1) : String.valueOf(calendar.get(Calendar.MONTH) + 1);

        return String.format("%s/%s/%s", day, month, calendar.get(Calendar.YEAR));
    }
}

