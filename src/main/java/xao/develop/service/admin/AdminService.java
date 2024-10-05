package xao.develop.service.admin;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import xao.develop.config.AdminCommand;
import xao.develop.config.AdminMessageLink;
import xao.develop.config.GeneralCommand;
import xao.develop.config.GeneralMessageLink;
import xao.develop.config.enums.TypeOfActionOfSelector;
import xao.develop.config.enums.TypeOfApp;
import xao.develop.config.enums.TypeOfAppStatus;
import xao.develop.service.BotService;

import java.util.*;

@Slf4j
@Service
public class AdminService implements GeneralCommand, GeneralMessageLink, AdminCommand, AdminMessageLink {

    @Autowired
    BotService service;

    @Autowired
    AdminMsgStart adminMsgStart;

    @Autowired
    AdminMsgNewApplications adminMsgNewApplications;

    @Autowired
    AdminMsgOpenApp adminMsgOpenApp;

    @Autowired
    AdminMsgArchive adminMsgArchive;

    @Autowired
    AdminMsgOpenArc adminMsgOpenArc;

    public void execute(Update update) {

        String[] data = service.getData(update).split(X);

        for (String d : data)
            log.debug("data: {}", d);

        List<Integer> messages = new ArrayList<>();

        try {
            if (update.hasMessage())
                processingMessage(update, messages, data[0]);
            else if (update.hasCallbackQuery())
                processingCallbackQuery(update, messages, data);
            else
                log.warn("Unknown data: {}", data[0]);

            for (Integer message : messages)
                service.registerMessage(service.getChatId(update), message);
        } catch (TelegramApiException ex) {
            log.error("Can't execute function: {}", ex.getMessage());
        }

        log.debug("Is the list of messages empty? {}", messages.isEmpty());
    }

    private void processingMessage(Update update, List<Integer> messages, String data) throws TelegramApiException {
        if (data.equals(START))
            start(update, messages, update.hasMessage());
        else
            log.info("Unknown message data: {}", data);

        service.deleteLastMessage(service.getChatId(update), service.getMessageId(update));
    }

    private void processingCallbackQuery(Update update, List<Integer> messages, String[] data) throws TelegramApiException {
        switch (data[0]) {
            case NEW_APPLICATIONS -> openListOfApps(update, messages, TypeOfApp.APP, false);
            case ARCHIVE -> openListOfApps(update, messages, TypeOfApp.ARC, false);

            case PREVIOUS_PAGE_OF_NEW_APPS -> changePage(update, messages, TypeOfApp.APP, TypeOfActionOfSelector.PREVIOUS);
            case NEXT_PAGE_OF_NEW_APPS -> changePage(update, messages, TypeOfApp.APP, TypeOfActionOfSelector.NEXT);
            case PREVIOUS_PAGE_OF_ARCHIVE -> changePage(update, messages, TypeOfApp.ARC, TypeOfActionOfSelector.PREVIOUS);
            case NEXT_PAGE_OF_ARCHIVE -> changePage(update, messages, TypeOfApp.ARC, TypeOfActionOfSelector.NEXT);

            case APP -> openApp(update, messages, data[1], TypeOfApp.APP);
            case ARC -> openApp(update, messages, data[1], TypeOfApp.ARC);

            case REFUSE_APP -> performActionStatement(update, messages, data[1], TypeOfAppStatus.DENIED);
            case ACCEPT_APP -> performActionStatement(update, messages, data[1], TypeOfAppStatus.ACCEPTED);
            case RETURN_APP -> performActionStatement(update, messages, data[1], TypeOfAppStatus.WAITING);

            case QUIT_FROM_APP -> openListOfApps(update, messages, TypeOfApp.APP, true);
            case QUIT_FROM_ARC -> openListOfApps(update, messages, TypeOfApp.ARC, true);

            case BACK_TO_START -> start(update, messages, update.hasMessage());

            case DELETE -> deleteMessage(update);

            default -> log.warn("Unknown callback query: {}", data[0]);
        }
    }

    private void start(Update update, List<Integer> messages, boolean isInit) throws TelegramApiException {
        if (isInit)
            service.authorization(update.getMessage());

        adminMsgStart.deleteAdminSettings(service.getChatId(update));
        adminMsgStart.editMessage(update, messages, ADMIN_MSG_START, service.getUser(update).getFirstName());
    }

    private void openListOfApps(Update update, List<Integer> messages, TypeOfApp type, boolean isBack) throws TelegramApiException {
        if (!isBack)
            adminMsgStart.createAdminSettings(service.getChatId(update));

        if (type.equals(TypeOfApp.APP))
            adminMsgNewApplications.editMessage(update, messages, ADMIN_MSG_NEW_APPS, adminMsgStart.getCountOfNewApps());
        else if (type.equals(TypeOfApp.ARC))
            adminMsgArchive.editMessage(update, messages, ADMIN_MSG_ARCHIVE, adminMsgStart.getCountOfArchive());
    }

    private void changePage(Update update,
                            List<Integer> messages,
                            TypeOfApp typeOfApp,
                            TypeOfActionOfSelector typeOfSelector) throws TelegramApiException {

        if (typeOfSelector.equals(TypeOfActionOfSelector.PREVIOUS))
            adminMsgStart.previousPage(service.getChatId(update));
        else if (typeOfSelector.equals(TypeOfActionOfSelector.NEXT))
            adminMsgStart.nextPage(service.getChatId(update));

        if (typeOfApp.equals(TypeOfApp.APP))
            adminMsgNewApplications.editMessage(update, messages, ADMIN_MSG_NEW_APPS, adminMsgStart.getCountOfNewApps());
        else if (typeOfApp.equals(TypeOfApp.ARC))
            adminMsgArchive.editMessage(update, messages, ADMIN_MSG_ARCHIVE, adminMsgStart.getCountOfArchive());
    }

    private void performActionStatement(Update update,
                                        List<Integer> messages,
                                        String data,
                                        TypeOfAppStatus typeOfAppStatus) throws TelegramApiException {

        int numOfApp = Integer.parseInt(data);

        adminMsgStart.updateBookingCardStatus(numOfApp, typeOfAppStatus);
        adminMsgStart.updateAdminSettings(service.getChatId(update), 0);

        if (typeOfAppStatus.equals(TypeOfAppStatus.WAITING))
            adminMsgArchive.editMessage(update, messages, ADMIN_MSG_ARCHIVE, adminMsgStart.getCountOfArchive());
        else
            adminMsgNewApplications.editMessage(update, messages, ADMIN_MSG_NEW_APPS, adminMsgStart.getCountOfNewApps());

        String status;
        long userId = adminMsgStart.getUserId(numOfApp);

        switch (typeOfAppStatus) {
            case WAITING -> status = service.getLocaleMessage(userId, ADMIN_MSG_STATUS_WAITING);
            case ACCEPTED -> status = service.getLocaleMessage(userId, ADMIN_MSG_STATUS_ACCEPTED);
            case DENIED -> status = service.getLocaleMessage(userId, ADMIN_MSG_STATUS_DENIED);
            case FINISHED -> status = service.getLocaleMessage(userId, ADMIN_MSG_STATUS_FINISHED);
            default -> status = "null";
        }

        service.sendMessageAdminUserUpdatedStatus(userId, GENERAL_MSG_UPDATED_STATUS,
                adminMsgStart.getIKMarkupUpdatedStatus(service.getChatId(update)), status);
    }

    private void openApp(Update update, List<Integer> messages, String data, TypeOfApp type) throws TelegramApiException {
        int numOfApp = Integer.parseInt(data);

        adminMsgStart.updateAdminSettings(service.getChatId(update), numOfApp);

        if (type.equals(TypeOfApp.APP))
            adminMsgOpenApp.editMessage(update, messages, ADMIN_MSG_APP, adminMsgStart.getAppParameters(update, numOfApp));
        else if (type.equals(TypeOfApp.ARC))
            adminMsgOpenArc.editMessage(update, messages, ADMIN_MSG_APP, adminMsgStart.getAppParameters(update, numOfApp));
    }

    private void deleteMessage(Update update) {
        service.deleteMessage(service.getChatId(update), service.getMessageId(update));
    }
}
