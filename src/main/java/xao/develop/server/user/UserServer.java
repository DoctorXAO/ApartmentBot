package xao.develop.server.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectFactory;
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
    UserMsgChooseCheckInDate userMsgChooseCheckInDate;
    @Autowired
    ObjectFactory<UserMsgChangeCheckInMonth> userMsgChangeCheckInMonth;
    @Autowired
    UserMsgChooseCheckOutDate userMsgChooseCheckOutDate;

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
            if (data.startsWith(RAA)) {
                processingRAA(update, messages, data);
            } else if (data.startsWith(HI)) {
                processingHI(update, messages, data);
            } else {
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

    private void processingRAA(Update update, List<Message> messages, String data) throws TelegramApiException {
        switch (data) {
            case RAA_RENT_AN_APARTMENT -> messages.add(userMsgRentAnApartment.sendMessage(update));

            case RAA_CHOOSE_CHECK_IN_DATE -> {
                userMsgChooseCheckInDate.addUserCalendar(update);
                messages.add(userMsgChooseCheckInDate.sendMessage(update));
            }
            case RAA_NEXT_CHECK_IN_YEAR -> {
                userMsgChooseCheckInDate.nextYear(update);
                update.getCallbackQuery().setData(RAA_CHOOSE_CHECK_IN_DATE);
                messages.add(userMsgChooseCheckInDate.sendMessage(update));
            }
            case RAA_PREVIOUS_CHECK_IN_YEAR -> {
                userMsgChooseCheckInDate.previousYear(update);
                update.getCallbackQuery().setData(RAA_CHOOSE_CHECK_IN_DATE);
                messages.add(userMsgChooseCheckInDate.sendMessage(update));
            }
            case RAA_NEXT_CHECK_IN_MONTH -> {
                userMsgChooseCheckInDate.nextMonth(update);
                update.getCallbackQuery().setData(RAA_CHOOSE_CHECK_IN_DATE);
                messages.add(userMsgChooseCheckInDate.sendMessage(update));
            }
            case RAA_PREVIOUS_CHECK_IN_MONTH -> {
                userMsgChooseCheckInDate.previousMonth(update);
                update.getCallbackQuery().setData(RAA_CHOOSE_CHECK_IN_DATE);
                messages.add(userMsgChooseCheckInDate.sendMessage(update));
            }
            case RAA_QUIT_FROM_CHOOSER_CHECK_IN -> {
                userMsgChooseCheckInDate.deleteUserCalendar(update);
                update.getCallbackQuery().setData(RAA_RENT_AN_APARTMENT);
                messages.add(userMsgRentAnApartment.sendMessage(update));
            }

            case RAA_CHOOSE_AN_APARTMENT -> {
                userMsgChooseAnApartment.addUserToSelector(update);
                messages = userMsgChooseAnApartment.sendPhotos(update,
                        "img/apartments/" + userMsgChooseAnApartment.getCurrentApartment(update).toString());
                messages.add(userMsgChooseAnApartment.sendMessage(update));
            }
            case RAA_NEXT_APARTMENT -> {
                userMsgChooseAnApartment.upSelector(update);
                update.getCallbackQuery().setData(RAA_CHOOSE_AN_APARTMENT);
                messages = userMsgChooseAnApartment.sendPhotos(update,
                        "img/apartments/" + userMsgChooseAnApartment.getCurrentApartment(update).toString());
                messages.add(userMsgChooseAnApartment.sendMessage(update));
            }
            case RAA_PREVIOUS_APARTMENT -> {
                userMsgChooseAnApartment.downSelector(update);
                update.getCallbackQuery().setData(RAA_CHOOSE_AN_APARTMENT);
                messages = userMsgChooseAnApartment.sendPhotos(update,
                        "img/apartments/" + userMsgChooseAnApartment.getCurrentApartment(update).toString());
                messages.add(userMsgChooseAnApartment.sendMessage(update));
            }
            case RAA_QUIT_FROM_CHOOSER_AN_APARTMENT -> {
                userMsgChooseAnApartment.deleteUserFromSelector(update);
                update.getCallbackQuery().setData(RAA_RENT_AN_APARTMENT);
                messages.add(userMsgRentAnApartment.sendMessage(update));
            }
            default -> log.info("Unknown RAA data: {}", data);
        }
    }

    private void processingHI(Update update, List<Message> messages, String data) throws TelegramApiException {
        switch (data) {
            case HI_HOUSE_INFORMATION -> messages.add(userMsgHouseInformation.sendMessage(update));
            case HI_RULES -> messages.add(userMsgRules.sendMessage(update));
            default -> log.info("Unknown HI data: {}", data);
        }
    }
}
