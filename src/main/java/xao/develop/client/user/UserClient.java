package xao.develop.client.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import xao.develop.config.UserCommand;
import xao.develop.client.Account;
import xao.develop.server.user.UserServer;

import java.util.List;

@Slf4j
@Component
public class UserClient implements Account, UserCommand {

    @Autowired
    UserServer server;

    @Override
    public void core(Update update) {
        log.trace("Method core(Update) started");

        server.deleteOldMessages(update);

        try {
            if (update.hasMessage() && update.getMessage().hasText())
                processingTheMessage(update);
            else if (update.hasCallbackQuery())
                processingTheCallbackQuery(update);
        } catch (Exception ex) {
            log.error("core: {}", ex.getMessage());
        }

        log.trace("Method core(Update) finished");
    }

    /** Обработка сообщений **/
    private void processingTheMessage(Update update) throws Exception {
        log.trace("Method processingTheMessage(Update) started");

        server.deleteLastMessages(update);

        String command = update.getMessage().getText();

        log.debug("User wrote the next command: {}", command);

        server.execute(update, command);

        log.trace("Method processingTheMessage(Update) finished");
    }

    private void processingTheCallbackQuery(Update update) throws Exception {
        log.trace("Method processingTheCallbackQuery(Update) started");

        String data = update.getCallbackQuery().getData();
        List<Message> messages;

        log.debug("processingTheCallbackQuery: variable data = {}", data);

//        switch (data) {
//            case APARTMENTS -> messages = data_apartments(update);
//            default -> throw new Exception("Unknown data: " + data);
//        }

        log.trace("Method processingTheCallbackQuery(Update) finished");
    }
}
