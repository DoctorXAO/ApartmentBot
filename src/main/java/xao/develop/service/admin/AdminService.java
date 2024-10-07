package xao.develop.service.admin;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import xao.develop.config.AdminCommand;
import xao.develop.config.AdminMessageLink;
import xao.develop.config.GeneralCommand;
import xao.develop.config.GeneralMessageLink;
import xao.develop.config.enums.Selector;
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

    @Autowired
    AdminMsgChangeLanguage adminMsgChangeLanguage;

    @Autowired
    AdminMsgChat adminMsgChat;

    public void execute(Update update) {
        log.trace("Method execute(Update, String) started");

        String[] data = service.getData(update).split(X, 2);

        for (String d : data)
            log.debug("data: {}", d);

        List<Integer> messages = new ArrayList<>();

        long chatId = service.getChatId(update);
        int msgId = service.getMessageId(update);
        User user = service.getUser(update);

        try {
            if (update.hasMessage())
                processingMessage(chatId, msgId, user, messages, data);
            else if (update.hasCallbackQuery())
                processingCallbackQuery(chatId, msgId, user, messages, data);
            else
                log.warn("Unknown data: {}", data[0]);

            for (Integer message : messages)
                service.registerMessage(chatId, message);
        } catch (TelegramApiException ex) {
            log.error("Can't execute function: {}", ex.getMessage());
        }

        log.trace("Method execute(Update, String) finished");
    }

    private void processingMessage(long chatId,
                                   int msgId,
                                   User user,
                                   List<Integer> messages,
                                   String[] data) throws TelegramApiException {
        switch (data[0]) {
            case START -> start(chatId, user, messages, true);
            case CHAT -> chat(chatId, data[1]);
            default -> log.info("Unknown message data: {}", data[0]);
        }

        deleteMessage(chatId, msgId);
    }

    private void processingCallbackQuery(long chatId,
                                         int msgId,
                                         User user,
                                         List<Integer> messages,
                                         String[] data) throws TelegramApiException {
        switch (data[0]) {
            case NEW_APPLICATIONS -> openListOfApps(chatId, messages, TypeOfApp.APP, false);
            case ARCHIVE -> openListOfApps(chatId, messages, TypeOfApp.ARC, false);
            case CHANGE_LANGUAGE -> openChangeLanguage(chatId, messages);

            case PREVIOUS_PAGE_OF_NEW_APPS -> changePage(chatId, messages, TypeOfApp.APP, Selector.PREVIOUS);
            case NEXT_PAGE_OF_NEW_APPS -> changePage(chatId, messages, TypeOfApp.APP, Selector.NEXT);
            case PREVIOUS_PAGE_OF_ARCHIVE -> changePage(chatId, messages, TypeOfApp.ARC, Selector.PREVIOUS);
            case NEXT_PAGE_OF_ARCHIVE -> changePage(chatId, messages, TypeOfApp.ARC, Selector.NEXT);

            case APP -> openApp(chatId, messages, data[1], TypeOfApp.APP);
            case ARC -> openApp(chatId, messages, data[1], TypeOfApp.ARC);

            case REFUSE_APP -> performActionStatement(chatId, messages, data[1], TypeOfAppStatus.DENIED);
            case ACCEPT_APP -> performActionStatement(chatId, messages, data[1], TypeOfAppStatus.ACCEPTED);
            case RETURN_APP -> performActionStatement(chatId, messages, data[1], TypeOfAppStatus.WAITING);

            case OPEN_CHAT -> openChat(chatId, messages, data[1]);

            case QUIT_FROM_APP -> openListOfApps(chatId, messages, TypeOfApp.APP, true);
            case QUIT_FROM_ARC -> openListOfApps(chatId, messages, TypeOfApp.ARC, true);

            case TR, EN, RU -> changeLanguage(chatId, user, messages, data[0]);

            case BACK_TO_START -> start(chatId, user, messages, false);

            case DELETE -> deleteMessage(chatId, msgId);

            default -> log.warn("Unknown callback query: {}", data[0]);
        }
    }

    private void start(long chatId,
                       User user,
                       List<Integer> messages,
                       boolean isInit) throws TelegramApiException {
        if (isInit)
            service.authorization(chatId, user);

        adminMsgStart.deleteAdminSettings(chatId);

        messages.add(adminMsgStart.editMessage(chatId, ADMIN_MSG_START, user.getFirstName()));
    }

    private void openListOfApps(long chatId, List<Integer> messages, TypeOfApp type, boolean isBack) throws TelegramApiException {
        if (!isBack)
            adminMsgStart.createAdminSettings(chatId);

        if (type.equals(TypeOfApp.APP))
            messages.add(adminMsgNewApplications.editMessage(chatId, ADMIN_MSG_NEW_APPS, adminMsgStart.getCountOfNewApps()));
        else if (type.equals(TypeOfApp.ARC))
            messages.add(adminMsgArchive.editMessage(chatId, ADMIN_MSG_ARCHIVE, adminMsgStart.getCountOfArchive()));
    }

    private void openChangeLanguage(long chatId, List<Integer> messages) throws TelegramApiException {
        messages.add(adminMsgChangeLanguage.editMessage(chatId, GENERAL_MSG_CHANGE_LANGUAGE));
    }

    private void changePage(long chatId,
                            List<Integer> messages,
                            TypeOfApp typeOfApp,
                            Selector typeOfSelector) throws TelegramApiException {

        if (typeOfSelector.equals(Selector.PREVIOUS))
            adminMsgStart.previousPage(chatId);
        else if (typeOfSelector.equals(Selector.NEXT))
            adminMsgStart.nextPage(chatId);

        if (typeOfApp.equals(TypeOfApp.APP))
            messages.add(adminMsgNewApplications.editMessage(chatId, ADMIN_MSG_NEW_APPS, adminMsgStart.getCountOfNewApps()));
        else if (typeOfApp.equals(TypeOfApp.ARC))
            messages.add(adminMsgArchive.editMessage(chatId, ADMIN_MSG_ARCHIVE, adminMsgStart.getCountOfArchive()));
    }

    private void performActionStatement(long chatId,
                                        List<Integer> messages,
                                        String data,
                                        TypeOfAppStatus typeOfAppStatus) throws TelegramApiException {

        int numOfApp = Integer.parseInt(data);

        adminMsgStart.updateBookingCardStatus(numOfApp, typeOfAppStatus);
        adminMsgStart.updateAdminSettings(chatId, 0);

        if (typeOfAppStatus.equals(TypeOfAppStatus.WAITING))
            messages.add(adminMsgArchive.editMessage(chatId, ADMIN_MSG_ARCHIVE, adminMsgStart.getCountOfArchive()));
        else
            messages.add(adminMsgNewApplications.editMessage(chatId, ADMIN_MSG_NEW_APPS, adminMsgStart.getCountOfNewApps()));

        String status;
        long userId = adminMsgStart.getUserId(numOfApp);

        switch (typeOfAppStatus) {
            case WAITING -> status = service.getLocaleMessage(userId, ADMIN_MSG_STATUS_WAITING);
            case ACCEPTED -> status = service.getLocaleMessage(userId, ADMIN_MSG_STATUS_ACCEPTED);
            case DENIED -> status = service.getLocaleMessage(userId, ADMIN_MSG_STATUS_DENIED);
            case FINISHED -> status = service.getLocaleMessage(userId, ADMIN_MSG_STATUS_FINISHED);
            default -> status = "null";
        }

        service.sendMessageAdminUser(userId, GENERAL_MSG_UPDATED_STATUS,
                adminMsgStart.getIKMarkupUpdatedStatus(chatId), status);
    }

    private void openChat(long chatId, List<Integer> messages, String data) throws TelegramApiException {
        int numOfApp = Integer.parseInt(data);

        messages.add(adminMsgChat.editMessage(chatId, ADMIN_MSG_CHAT, adminMsgStart.getAppParameters(chatId, numOfApp)));
    }

    private void openApp(long chatId, List<Integer> messages, String data, TypeOfApp type) throws TelegramApiException {
        int numOfApp = Integer.parseInt(data);

        adminMsgStart.updateAdminSettings(chatId, numOfApp);

        if (type.equals(TypeOfApp.APP))
            messages.add(adminMsgOpenApp.editMessage(chatId, ADMIN_MSG_APP, adminMsgStart.getAppParameters(chatId, numOfApp)));
        else if (type.equals(TypeOfApp.ARC))
            messages.add(adminMsgOpenArc.editMessage(chatId, ADMIN_MSG_APP, adminMsgStart.getAppParameters(chatId, numOfApp)));
    }

    private void changeLanguage(long chatId,
                                User user,
                                List<Integer> messages,
                                String data) throws TelegramApiException {
        service.setLanguage(chatId, data);
        start(chatId, user, messages, false);
    }

    private void chat(long chatId, String data) throws TelegramApiException {
        long userId = adminMsgStart.getUserId(adminMsgStart.getSelectedApp(chatId));

        service.sendMessageAdminUser(userId, ADMIN_MSG_CHATTING_ADMIN, adminMsgStart.getIKMarkupChat(userId), data);

        service.sendMessageAdminUser(chatId, ADMIN_MSG_SENT_SUCCESSFULLY, adminMsgStart.getIKMarkupChat(chatId), data);
    }

    private void deleteMessage(long chatId, int msgId) {
        service.deleteMessage(chatId, msgId);
    }
}
