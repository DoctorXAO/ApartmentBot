package xao.develop.server.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import xao.develop.config.UserCommand;
import xao.develop.server.Server;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class UserServer implements UserCommand {

    @Autowired
    UserMsgStart userMsgStart;

    @Autowired
    UserMsgApartments userMsgApartments;

    @Autowired
    UserMsgRentAnApartment userMsgRentAnApartment;
    @Autowired
    UserMsgChooseAnApartment userMsgChooseAnApartment;

    @Autowired
    UserMsgHouseInformation userMsgHouseInformation;
    @Autowired
    UserMsgRules userMsgRules;

    @Autowired
    UserMsgContacts userMsgContacts;

    @Autowired
    UserMsgChangeLanguage userMsgChangeLanguage;

    @Autowired
    Server server;

    public void execute(Update update) {
        log.trace("Method execute(Update, String) started");

        String data = server.getData(update);

        log.debug("Data: {}", data);

        List<Message> messages = new ArrayList<>();

        try {
            switch (data) {
                case START -> {
                    server.authorization(update.getMessage());
                    server.deleteLastMessage(update);
                    messages.add(userMsgStart.sendMessage(update));
                }
                case APARTMENTS -> {
                    messages = userMsgApartments.sendPhotos(update, "img/simple_apartment");
                    messages.add(userMsgApartments.sendMessage(update));
                }
                case RENT_AN_APARTMENT -> messages.add(userMsgRentAnApartment.sendMessage(update));
                case CHOOSE_AN_APARTMENT -> {
                    userMsgChooseAnApartment.addUserToSelector(update);
                    messages = userMsgChooseAnApartment.sendPhotos(update,
                            "img/apartments/" + userMsgChooseAnApartment.getCurrentApartment(update).toString());
                    messages.add(userMsgChooseAnApartment.sendMessage(update));
                }
                case NEXT_APARTMENT -> {
                    userMsgChooseAnApartment.upSelector(update);
                    update.getCallbackQuery().setData(CHOOSE_AN_APARTMENT);
                    messages = userMsgChooseAnApartment.sendPhotos(update,
                            "img/apartments/" + userMsgChooseAnApartment.getCurrentApartment(update).toString());
                    messages.add(userMsgChooseAnApartment.sendMessage(update));
                }
                case BACK_APARTMENT -> {
                    userMsgChooseAnApartment.downSelector(update);
                    update.getCallbackQuery().setData(CHOOSE_AN_APARTMENT);
                    messages = userMsgChooseAnApartment.sendPhotos(update,
                            "img/apartments/" + userMsgChooseAnApartment.getCurrentApartment(update).toString());
                    messages.add(userMsgChooseAnApartment.sendMessage(update));
                }
                case QUIT_FROM_CHOOSER -> {
                    userMsgChooseAnApartment.deleteUserFromSelector(update);
                    update.getCallbackQuery().setData(RENT_AN_APARTMENT);
                    messages.add(userMsgRentAnApartment.sendMessage(update));
                }
                case HOUSE_INFORMATION -> messages.add(userMsgHouseInformation.sendMessage(update));
                case RULES -> messages.add(userMsgRules.sendMessage(update));
                case CONTACTS -> messages.add(userMsgContacts.sendMessage(update));
                case CHANGE_LANGUAGE -> messages.add(userMsgChangeLanguage.sendMessage(update));
                case BACK_TO_START -> {
                    update.getCallbackQuery().setData(START);
                    messages.add(userMsgStart.sendMessage(update));
                }
                case EN, TR, RU -> {
                    server.setLanguage(update, data);
                    update.getCallbackQuery().setData("/start");
                    messages.add(userMsgStart.sendMessage(update));
                }
                default -> log.info("Unknown data: {}", data);
            }

            if (update.hasMessage() && !update.getMessage().getText().startsWith("/"))
                server.deleteLastMessage(update);

            for (Message message : messages)
                server.registerMessage(server.getChatId(update), message.getMessageId());
        } catch (TelegramApiException ex) {
            log.error("execute: {}", ex.getMessage());
        }

        log.trace("Method execute(Update, String) finished");
    }
}
