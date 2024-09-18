package xao.develop.server;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import xao.develop.config.BotConfig;
import xao.develop.model.TempUserMessage;
import xao.develop.repository.Persistence;

import java.util.List;

@Slf4j
@Service
public class Server {

    @Autowired
    BotConfig botConfig;

    @Autowired
    Persistence persistence;

    public void setLanguage(Update update, String language) {
        persistence.updateUserStatusLanguage(getChatId(update), language);
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
        String login = message.getFrom().getUserName();
        String firstName = message.getFrom().getFirstName();
        String lastName = message.getFrom().getLastName();
        String language = message.getFrom().getLanguageCode();

        persistence.insertUserStatus(chatId, login, firstName, lastName, language, 0);

        log.debug("""
                Authorization: the following user parameters have been added to the database:
                chatId = {}
                login = {}
                firstName = {}
                lastName = {}
                language = {}
                """, chatId, login, firstName, lastName, language);

        log.trace("Method authorization(Message) finished");
    }

    public void deleteOldMessages(Update update) {
        log.trace("Method 'deleteOldMessage' started for userID: {}", getChatId(update));

        Long chatId = getChatId(update);

        List<TempUserMessage> userMessages = persistence.selectUserMessages(chatId);

        try {
            for(TempUserMessage userMessage : userMessages) {
                DeleteMessage deleteMessage = DeleteMessage
                        .builder()
                        .chatId(userMessage.getChatId())
                        .messageId(userMessage.getMsgId())
                        .build();

                botConfig.getTelegramClient().execute(deleteMessage);
                log.debug("MessageID {} deleted", userMessage.getMsgId());
            }

            persistence.deleteUserMessage(chatId);
        } catch (TelegramApiException ex) {
            log.warn("""
                    Message to delete not found for userID: {}
                    Exception: {}""",
                    chatId, ex.getMessage());
        }

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

    public void registerMessage(Long chatId, int messageId) {
        persistence.insertUserMessage(chatId, messageId);
    }
}
