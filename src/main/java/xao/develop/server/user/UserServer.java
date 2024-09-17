package xao.develop.server.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import xao.develop.config.BotConfig;
import xao.develop.config.UserCommand;
import xao.develop.model.TempUserMessage;
import xao.develop.repository.UserPersistence;
import xao.develop.server.Server;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class UserServer implements UserCommand {

    @Autowired
    BotConfig botConfig;

    @Autowired
    UserPersistence userPersistence;

    @Autowired
    UserMessageStart userMessageStart;

    @Autowired
    UserMessageApartment userMessageApartment;

    @Autowired
    Server server;

    public void execute(Update update, String data) {
        log.trace("Method execute(Update, String) started");

        List<Message> messages = new ArrayList<>();

        try {
            switch (data) {
                case START -> {
                    authorization(update.getMessage());
                    messages.add(userMessageStart.sendMessage(update));
                }
                case APARTMENTS -> {
                    messages = userMessageApartment.testSendMessage(update);
                    messages.add(userMessageApartment.sendMessage(update));
                }
                default -> throw new Exception("Unknown data: " + data);
            }

            for (Message message : messages)
                registerMessage(server.getChatId(update), message.getMessageId());
        } catch (Exception ex) {
            log.error("execute: {}", ex.getMessage());
        }

        log.trace("Method execute(Update, String) finished");
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
        log.trace("Method 'deleteOldMessage' started for userID: {}", server.getChatId(update));

        Long chatId = server.getChatId(update);

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
        registerMessage(server.getChatId(update), server.getMessageId(update));
        deleteOldMessages(update);
    }

    public void registerMessage(Long chatId, int messageId) {
        userPersistence.insertUserMessage(chatId, messageId);
    }
}
