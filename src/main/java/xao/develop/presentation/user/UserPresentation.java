package xao.develop.presentation.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import xao.develop.config.BotConfig;
import xao.develop.config.UserCommand;
import xao.develop.presentation.Account;
import xao.develop.service.user.UserMainIK;
import xao.develop.service.user.UserService;

@Slf4j
@Component
public class UserPresentation implements Account, UserCommand {

    @Autowired
    BotConfig botConfig;

    @Autowired
    UserService service;

    @Autowired
    UserMainIK userMainIK;

    @Override
    public void core(Update update) {
        log.trace("Method 'core' started");

        service.deleteOldMessages(update);

        try {
            if (update.hasMessage() && update.getMessage().hasText())
                processingTheMessage(update);
        } catch (TelegramApiException ex) {
            log.error("error: {}", ex.getMessage());
        }

        log.trace("Method 'core' finished");
    }

    /** Обработка сообщений **/
    private void processingTheMessage(Update update) throws TelegramApiException {
        log.trace("Method 'processingTheMessage' started");

        service.deleteLastMessages(update);

        if (update.getMessage().getText().equals(START)) {
            log.debug("User wrote the '/start' command");

            Message message = cmd_start(update);
            service.registerMessage(message.getChatId(), message.getMessageId());
        }

        log.trace("Method 'processingTheMessage' finished");
    }

    /** Получена команда /start **/
    private Message cmd_start(Update update) throws TelegramApiException {
        log.trace("Method 'cmd_start' started");

        service.authorization(update.getMessage());

        log.trace("Method 'cmd_start' finished");

        return botConfig.getTelegramClient().execute(service.buildSendMessage(update,
                service.getLocalizationText(update),
                userMainIK.getMainIKMarkup(update)));
    }
}
