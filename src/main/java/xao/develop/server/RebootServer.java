package xao.develop.server;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import xao.develop.config.BotConfig;
import xao.develop.config.UserCommand;
import xao.develop.model.AccountStatus;
import xao.develop.model.TempBotMessage;
import xao.develop.repository.Persistence;
import xao.develop.server.user.UserLocalization;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class RebootServer implements UserCommand {

    @Autowired
    BotConfig botConfig;

    @Autowired
    Persistence persistence;

    @Autowired
    MessageBuilder msgBuilder;

    @Autowired
    UserLocalization userLoc;

    @Scheduled(cron = "0 0 0 * * ?")
    @EventListener(ContextRefreshedEvent.class)
    public void setPresentTime() {
        persistence.setPresentTime();
    }

    @EventListener(ContextRefreshedEvent.class)
    public void clearTempDAO() {
        persistence.clearTempDAO();
    }

    @EventListener(ContextRefreshedEvent.class)
    public void clearAllMessages() {
        List<Long> chatIds = persistence.selectDistinctChatIdsTempBotMessages();
        for (long chatId : chatIds) {
            List<TempBotMessage> tempBotMessages = persistence.selectTempBotMessages(chatId);

            for (int i = 0; i < tempBotMessages.size(); i++) {
                TempBotMessage tempBotMessage = tempBotMessages.get(i);

                if (i == tempBotMessages.size() - 1 && chatId != botConfig.getAdminId()) {
                    try {
                        Update update = new Update();

                        CallbackQuery callbackQuery = new CallbackQuery();
                        Message message = new Message();
                        Chat chat = new Chat(chatId, "null");

                        callbackQuery.setData(START);
                        message.setChat(chat);

                        update.setCallbackQuery(callbackQuery);
                        update.getCallbackQuery().setMessage(message);

                        EditMessageText editMessageText = EditMessageText
                                .builder()
                                .chatId(chatId)
                                .messageId(tempBotMessage.getMsgId())
                                .text(userLoc.getLocalizationText(update))
                                .replyMarkup(getStartIKMarkup(update))
                                .parseMode("HTML")
                                .build();

                        botConfig.getTelegramClient().execute(editMessageText);

                        log.debug("User {} message {} edited", tempBotMessage.getChatId(), tempBotMessage.getMsgId());

                        break;
                    } catch (TelegramApiException ex) {
                        log.error("""
                                        [Reboot] Impossible to edit user {} message {}
                                        Exception: {}""",
                                tempBotMessage.getChatId(), tempBotMessage.getMsgId(), ex.getMessage());

                        break;
                    }
                }

                try {
                    DeleteMessage deleteMessage = DeleteMessage
                            .builder()
                            .chatId(tempBotMessage.getChatId())
                            .messageId(tempBotMessage.getMsgId())
                            .build();

                    botConfig.getTelegramClient().execute(deleteMessage);

                    log.debug("User {} message {} deleted", tempBotMessage.getChatId(), tempBotMessage.getMsgId());
                } catch (TelegramApiException ex) {
                    log.warn("""
                                    [Reboot] Impossible to delete message {} for user {}
                                    Exception: {}""",
                            tempBotMessage.getMsgId(), tempBotMessage.getChatId(), ex.getMessage());
                }
            }
        }
    }

    public InlineKeyboardMarkup getStartIKMarkup(Update update) {
        List<InlineKeyboardRow> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> buttons = new ArrayList<>();

        buttons.add(msgBuilder.buildIKButton(
                userLoc.getLocalizationButton(update, UserCommand.RAA_CHOOSE_CHECK_DATE), RAA_CHOOSE_CHECK_DATE));
        keyboard.add(msgBuilder.buildIKRow(buttons));
        buttons.clear();

        buttons.add(msgBuilder.buildIKButton(userLoc.getLocalizationButton(update, ABOUT_US), ABOUT_US));
        keyboard.add(msgBuilder.buildIKRow(buttons));
        buttons.clear();

        buttons.add(msgBuilder.buildIKButton(userLoc.getLocalizationButton(update, CONTACTS), CONTACTS));
        keyboard.add(msgBuilder.buildIKRow(buttons));
        buttons.clear();

        buttons.add(msgBuilder.buildIKButton(userLoc.getLocalizationButton(update, CHANGE_LANGUAGE), CHANGE_LANGUAGE));
        keyboard.add(msgBuilder.buildIKRow(buttons));

        return InlineKeyboardMarkup
                .builder()
                .keyboard(keyboard)
                .build();
    }
}