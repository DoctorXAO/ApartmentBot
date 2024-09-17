package xao.develop.client.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import xao.develop.config.UserCommand;
import xao.develop.client.Account;
import xao.develop.server.user.UserServer;

@Slf4j
@Component
public class UserClient implements Account, UserCommand {

    @Autowired
    UserServer userServer;

    @Override
    public void core(Update update) {
        log.trace("Method core(Update) started");

        userServer.deleteOldMessages(update);

        if (update.hasMessage() && update.getMessage().hasText())
            processingTheMessage(update);
        else if (update.hasCallbackQuery())
            processingTheCallbackQuery(update);

        log.trace("Method core(Update) finished");
    }

    private void processingTheMessage(Update update) {
        log.trace("Method processingTheMessage(Update) started");

        String command = update.getMessage().getText();

        log.debug("User wrote the next command: {}", command);

        userServer.deleteLastMessages(update);
        userServer.execute(update, command);

        log.trace("Method processingTheMessage(Update) finished");
    }

    private void processingTheCallbackQuery(Update update) {
        log.trace("Method processingTheCallbackQuery(Update) started");

        String data = update.getCallbackQuery().getData();

        log.debug("processingTheCallbackQuery: variable data = {}", data);

        userServer.execute(update, data);

        log.trace("Method processingTheCallbackQuery(Update) finished");
    }
}
