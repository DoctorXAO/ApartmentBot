package xao.develop.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import xao.develop.command.*;
import xao.develop.config.BotConfig;
import xao.develop.model.TempBotMessage;
import xao.develop.repository.Persistence;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class BotRebootService implements UserCommand, UserMessageLink, GeneralCommand, GeneralMessageLink {

    @Autowired
    BotConfig botConfig;

    @Autowired
    Persistence persistence;

    @Autowired
    MessageBuilder msgBuilder;

    @Autowired
    BotService service;

    @Scheduled(cron = "0 0 0 * * ?")
    @EventListener(ContextRefreshedEvent.class)
    public void setPresentTime() {
        persistence.setPresentTime();
    }

    @Scheduled(cron = "0 30 14 * * ?")
    @EventListener(ContextRefreshedEvent.class)
    public void freeUpTheVacatedApartments() {
        persistence.freeUpTheVacatedApartments();
    }

    @EventListener(ContextRefreshedEvent.class)
    public void clearTempDAO() {
        persistence.clearTempDAO();
        persistence.resetToDefaultTempAdminSettings();
    }

    @EventListener(ContextRefreshedEvent.class)
    public void clearAllMessages() {
        List<Long> chatIds = persistence.selectDistinctChatIdsTempBotMessages();

        for (long chatId : chatIds) {
            List<TempBotMessage> tempBotMessages = persistence.selectTempBotMessages(chatId);

            for (int i = 0; i < tempBotMessages.size(); i++) {
                TempBotMessage tempBotMessage = tempBotMessages.get(i);

                if (i == tempBotMessages.size() - 1) {
                    editMessage(tempBotMessage);
                } else
                    deleteMessage(tempBotMessage);
            }
        }
    }

    private void editMessage(TempBotMessage tempBotMessage) {
        try {
            long chatId = tempBotMessage.getChatId();
            int msgId = tempBotMessage.getMsgId();

            botConfig.getTelegramClient().execute(
                    service.editMessageText(chatId,
                            msgId,
                            service.getLocaleMessage(chatId, GENERAL_MSG_REBOOTED),
                            getRebootIKMarkup(chatId)));

            log.debug("ChatId {} message {} edited", tempBotMessage.getChatId(), tempBotMessage.getMsgId());
        } catch (TelegramApiException ex) {
            log.warn("""
                                        [Reboot] Impossible to edit chatId {} message {}
                                        Exception: {}""",
                    tempBotMessage.getChatId(), tempBotMessage.getMsgId(), ex.getMessage());
        }
    }

    private void deleteMessage(TempBotMessage tempBotMessage) {
        try {
            DeleteMessage deleteMessage = DeleteMessage
                    .builder()
                    .chatId(tempBotMessage.getChatId())
                    .messageId(tempBotMessage.getMsgId())
                    .build();

            botConfig.getTelegramClient().execute(deleteMessage);

            log.debug("ChatId {} message {} deleted", tempBotMessage.getChatId(), tempBotMessage.getMsgId());
        } catch (TelegramApiException ex) {
            log.warn("""
                                    [Reboot] Impossible to delete message {} for chatId {}
                                    Exception: {}""",
                    tempBotMessage.getMsgId(), tempBotMessage.getChatId(), ex.getMessage());
        }
    }

    private InlineKeyboardMarkup getRebootIKMarkup(long chatId) {
        List<InlineKeyboardRow> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> buttons = new ArrayList<>();

        buttons.add(msgBuilder.buildIKButton(service.getLocaleMessage(chatId, GENERAL_BT_START), BACK_TO_START));
        keyboard.add(msgBuilder.buildIKRow(buttons));

        return InlineKeyboardMarkup
                .builder()
                .keyboard(keyboard)
                .build();
    }
}