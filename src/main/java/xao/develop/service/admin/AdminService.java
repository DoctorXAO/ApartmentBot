package xao.develop.service.admin;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import xao.develop.config.AdminCommand;
import xao.develop.config.AdminMessageLink;
import xao.develop.service.BotService;

import java.util.*;

@Slf4j
@Service
public class AdminService implements AdminCommand, AdminMessageLink {

    @Autowired
    BotService service;

    @Autowired
    AdminMsgStart adminMsgStart;

    @Autowired
    AdminMsgNewApplications adminMsgNewApplications;

    @Autowired
    AdminMsgApplication adminMsgApplication;

    public void execute(Update update) {

        String data = service.getData(update);

        log.debug("Data: {}", data);

        List<Integer> messages = new ArrayList<>();

        try {
            if (update.hasMessage())
                processingMessage(update, messages, data);
            else if (data.startsWith(APP_))
                adminMsgApplication.editMessage(update, messages, ADMIN_MSG_APPLICATION,
                        adminMsgApplication.getParameters(Long.parseLong(data.replaceAll(APP_, ""))));
            else if (update.hasCallbackQuery())
                processingCallbackQuery(update, messages, data);
            else
                log.warn("Unknown data: {}", data);
        } catch (TelegramApiException ex) {
            log.error("Can't execute function: {}", ex.getMessage());
        }

        log.debug("Is the list of messages empty? {}", messages.isEmpty());

        for (Integer message : messages)
            service.registerMessage(service.getChatId(update), message);
    }

    private void processingMessage(Update update, List<Integer> messages, String data) throws TelegramApiException {
        if (data.equals(START)) {
            service.authorization(update.getMessage());
            adminMsgStart.editMessage(update, messages, ADMIN_MSG_START, service.getUser(update).getFirstName());
        } else
            log.info("Unknown message data: {}", data);

        service.deleteLastMessage(service.getChatId(update), service.getMessageId(update));
    }

    private void processingCallbackQuery(Update update, List<Integer> messages, String data) throws TelegramApiException {
        switch (data) {
            case NEW_APPLICATIONS -> adminMsgNewApplications.editMessage(update, messages, ADMIN_MSG_NEW_APPLICATIONS,
                    adminMsgNewApplications.getCountOfNewApplications());
            case BACK_TO_START -> adminMsgStart.editMessage(update, messages, ADMIN_MSG_START,
                    service.getUser(update).getFirstName());
        }
    }
}
