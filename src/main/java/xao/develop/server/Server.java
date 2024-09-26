package xao.develop.server;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import xao.develop.config.BotConfig;
import xao.develop.model.TempBotMessage;
import xao.develop.repository.Persistence;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class Server {

    @Autowired
    BotConfig botConfig;

    @Autowired
    Persistence persistence;

    public void setLanguage(Update update, String language) {
        persistence.updateLanguageInAccountStatus(getChatId(update), language);
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

    public String getData(Update update) {
        log.trace("Method getData(Update) started and finished");

        if (update.hasMessage())
            return update.getMessage().getText();
        else
            return update.getCallbackQuery().getData();
    }

    public void authorization(Message message) {
        log.trace("Method authorization(Message) started");

        Long chatId = message.getChatId();
        String language = message.getFrom().getLanguageCode();

        persistence.insertAccountStatus(chatId, language);

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

    public void deleteLastMessage(Update update) {
        log.trace("Method deleteLastMessage(Update) started for userID: {}", getChatId(update));

        try {
            botConfig.getTelegramClient().execute(DeleteMessage
                    .builder()
                    .chatId(getChatId(update))
                    .messageId(getMessageId(update))
                    .build());

            log.debug("Last messageID {} deleted", getMessageId(update));
        } catch (TelegramApiException ex) {
            log.warn("""
                    Can't delete the user message with userID: {}
                    Exception: {}""",
                    getChatId(update), ex.getMessage());
        }

        log.trace("Method deleteLastMessage(Update) finished for userID: {}", getChatId(update));
    }

    public void deleteUserFromTempBookingData(Update update) {
        persistence.deleteTempBookingData(getChatId(update));

        log.debug("The next user from UserCalendar deleted: {}", getChatId(update));
    }

    public void registerMessage(Long chatId, int messageId) {
        persistence.insertTempBotMessage(chatId, messageId);
        log.debug("The message {} registered", messageId);
    }
}
