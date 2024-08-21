package xao.develop.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import xao.develop.config.BotConfig;
import xao.develop.model.TempUserMessage;
import xao.develop.model.TempUserMessagesRepository;

import java.util.List;

@Service
public class UserService implements LongPollingSingleThreadUpdateConsumer {

    @Autowired
    private TempUserMessagesRepository tempUserMessagesRepository;

    @Autowired
    private BotConfig botConfig;

    @Autowired
    private CommandService commandService;

    @Override
    public void consume(Update update) {
        if ((update.hasMessage() && update.getMessage().hasText()) || update.hasCallbackQuery())
            deleteOldMessages(update);

        Message message = new Message();

        if (update.hasMessage() && update.getMessage().hasText()) {
            String msg = update.getMessage().getText();

            try {
                registerMessage(update.getMessage().getChatId(), update.getMessage().getMessageId());
                deleteOldMessages(update);

                if (msg.equals("/start")) {
                    message = commandService.cmd_start(update);
                    registerMessage(message.getChatId(), message.getMessageId());
                }
            } catch (TelegramApiException ex) {
                ex.printStackTrace(); // todo Добавь логи
            }
        } else if (update.hasCallbackQuery()) {
            String data = update.getCallbackQuery().getData();

            try {
                switch (data) {
                    case "available_apartments" -> message = commandService.data_apartments(update);
                    case "rent_an_apartment" -> message = commandService.data_rent_an_apartment(update);
                    case "house_information",
                         "back_from_rules" -> message = commandService.data_house_information(update);
                    case "contacts" -> message = commandService.data_contacts(update);
                    case "rules" -> message = commandService.data_rules(update);
                    case "back_from_contacts",
                         "back_from_house_information",
                         "back_from_apartments",
                         "back_from_rent_an_apartment" -> message = commandService.cmd_start(update);
                    default -> commandService.dataIsNotRecognized();
                }

                registerMessage(message.getChatId(), message.getMessageId());
            } catch (TelegramApiException ex) {
                ex.printStackTrace(); // todo Добавь логи
            }
        }
    }

    private void deleteOldMessages(Update update) {
        Long chatId;

        if (update.hasMessage())
            chatId = update.getMessage().getChatId();
        else
            chatId = update.getCallbackQuery().getMessage().getChatId();

        List<TempUserMessage> userMessages = getAllUserMessages(chatId);

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
            tempUserMessagesRepository.deleteByChatId(chatId);
        } catch (InvalidDataAccessApiUsageException ex) {
            System.out.println("No messages to delete");
        }
    }

    private List<TempUserMessage> getAllUserMessages(Long chatID) {
        return tempUserMessagesRepository.findByChatId(chatID);
    }

    private void registerMessage(long chatID, int msgID) {
        TempUserMessage tempUserMessage = new TempUserMessage();
        tempUserMessage.setChatId(chatID);
        tempUserMessage.setMsgId(msgID);

        tempUserMessagesRepository.save(tempUserMessage);
    }
}
