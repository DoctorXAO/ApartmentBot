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
    UserMsgChooseAnApartment userMsgChooseAnApartment;
    @Autowired
    UserMsgChooseCheckDate userMsgChooseCheckDate;
    @Autowired
    UserMsgChangeCheckInMonth userMsgChangeCheckInMonth;
    @Autowired
    UserMsgChangeCheckInYear userMsgChangeCheckInYear;

    @Autowired
    UserMsgAboutUs userMsgAboutUs;

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
            } else
                switch (data) {
                    case START -> {
                        server.authorization(update.getMessage());
                        messages.add(userMsgStart.sendMessage(update));
                    }
                    case ABOUT_US -> messages.add(userMsgAboutUs.sendMessage(update));
                    case CONTACTS -> messages.add(userMsgContacts.sendMessage(update));
                    case CHANGE_LANGUAGE -> messages.add(userMsgChangeLanguage.sendMessage(update));
                    case BACK_TO_START -> {
                        update.getCallbackQuery().setData(START);
                        messages.add(userMsgStart.sendMessage(update));
                    }
                    case EN, TR, RU -> {
                        server.setLanguage(update, data);
                        update.getCallbackQuery().setData(START);
                        messages.add(userMsgStart.sendMessage(update));
                    }
                    default -> log.info("Unknown data: {}", data);
                }

            if (update.hasMessage() && !update.getMessage().getText().startsWith("/"))
                server.deleteLastMessage(update);

            log.debug("Is the list of messages empty? {}", messages.isEmpty());

            for (Message message : messages)
                server.registerMessage(server.getChatId(update), message.getMessageId());
        } catch (TelegramApiException ex) {
            log.error("execute: {}", ex.getMessage());
        }

        log.trace("Method execute(Update, String) finished");
    }

    private void processingRAA(Update update, List<Message> messages, String data) throws TelegramApiException {
        if (data.startsWith(RAA + SET)) {
            processingRAA_SET(update, messages, data);
        } else
            switch (data) {
                case RAA_CHOOSE_CHECK_DATE -> {
                    userMsgChooseCheckDate.addNewUserToTempBookingData(update);
                    messages.add(userMsgChooseCheckDate.sendMessage(update));
                }
                case RAA_CHANGE_CHECK_IN_YEAR -> messages.add(userMsgChangeCheckInYear.sendMessage(update));
                case RAA_CHANGE_CHECK_IN_MONTH -> messages.add(userMsgChangeCheckInMonth.sendMessage(update));
                case RAA_NEXT_CHECK_YEAR_CM -> {
                    userMsgChooseCheckDate.nextYear(update);
                    update.getCallbackQuery().setData(RAA_CHANGE_CHECK_IN_MONTH);
                    messages.add(userMsgChangeCheckInMonth.sendMessage(update));
                }
                case RAA_PREVIOUS_CHECK_YEAR_CM -> {
                    userMsgChooseCheckDate.previousYear(update);
                    update.getCallbackQuery().setData(RAA_CHANGE_CHECK_IN_MONTH);
                    messages.add(userMsgChangeCheckInMonth.sendMessage(update));
                }
                case RAA_QUIT_FROM_CHANGE_CHECK_MONTH -> {
                    update.getCallbackQuery().setData(RAA_CHOOSE_CHECK_DATE);
                    messages.add(userMsgChooseCheckDate.sendMessage(update));
                }
                case RAA_NEXT_CHECK_YEAR -> {
                    userMsgChooseCheckDate.nextYear(update);
                    update.getCallbackQuery().setData(RAA_CHOOSE_CHECK_DATE);
                    messages.add(userMsgChooseCheckDate.sendMessage(update));
                }
                case RAA_PREVIOUS_CHECK_YEAR -> {
                    userMsgChooseCheckDate.previousYear(update);
                    update.getCallbackQuery().setData(RAA_CHOOSE_CHECK_DATE);
                    messages.add(userMsgChooseCheckDate.sendMessage(update));
                }
                case RAA_NEXT_CHECK_MONTH -> {
                    userMsgChooseCheckDate.nextMonth(update);
                    update.getCallbackQuery().setData(RAA_CHOOSE_CHECK_DATE);
                    messages.add(userMsgChooseCheckDate.sendMessage(update));
                }
                case RAA_PREVIOUS_CHECK_MONTH -> {
                    userMsgChooseCheckDate.previousMonth(update);
                    update.getCallbackQuery().setData(RAA_CHOOSE_CHECK_DATE);
                    messages.add(userMsgChooseCheckDate.sendMessage(update));
                }
                case RAA_QUIT_FROM_CHOOSER_CHECK -> {
                    if (userMsgChooseCheckDate.isCheckInSet(update)) {
                        userMsgChooseCheckDate.deleteCheckIn(update);
                        update.getCallbackQuery().setData(RAA_CHOOSE_CHECK_DATE);
                        messages.add(userMsgChooseCheckDate.sendMessage(update));
                    } else {
                        server.deleteUserFromTempBookingData(update);
                        update.getCallbackQuery().setData(START);
                        messages.add(userMsgStart.sendMessage(update));
                    }
                }
                case RAA_NEXT_APARTMENT -> {
                    userMsgChooseAnApartment.upSelector(update);
                    update.getCallbackQuery().setData(RAA_CHOOSE_AN_APARTMENT);
                    messages.addAll(userMsgChooseAnApartment.sendPhotos(update,
                            "img/apartments/" + userMsgChooseAnApartment.getCurrentApartment(update).toString()));
                    messages.add(userMsgChooseAnApartment.sendMessage(update));
                }
                case RAA_PREVIOUS_APARTMENT -> {
                    userMsgChooseAnApartment.downSelector(update);
                    update.getCallbackQuery().setData(RAA_CHOOSE_AN_APARTMENT);
                    messages.addAll(userMsgChooseAnApartment.sendPhotos(update,
                            "img/apartments/" + userMsgChooseAnApartment.getCurrentApartment(update).toString()));
                    messages.add(userMsgChooseAnApartment.sendMessage(update));
                }
                case RAA_QUIT_FROM_CHOOSER_AN_APARTMENT -> {
                    userMsgChooseAnApartment.deleteUserFromSelector(update);
                    userMsgChooseCheckDate.deleteCheckOut(update);
                    update.getCallbackQuery().setData(RAA_CHOOSE_CHECK_OUT_DATE);
                    messages.add(userMsgChooseCheckDate.sendMessage(update));
                }
                default -> log.info("Unknown RAA data: {}", data);
            }
    }

    private void processingRAA_SET(Update update, List<Message> messages, String data) throws TelegramApiException {
        if (data.startsWith(RAA_SET_YEAR)) {
            userMsgChangeCheckInYear.setYear(update, Integer.parseInt(data.replaceAll(RAA_SET_YEAR, "")));
            processingRAA_SET_sendMessage(update, messages);
        } else if (data.startsWith(RAA_SET_DAY)) {
            if (!userMsgChooseCheckDate.isCheckInSet(update)) {
                userMsgChooseCheckDate.setCheckIn(update, Integer.parseInt(data.replaceAll(RAA_SET_DAY, "")));
                update.getCallbackQuery().setData(RAA_CHOOSE_CHECK_OUT_DATE);
                messages.add(userMsgChooseCheckDate.sendMessage(update));
            } else {
                userMsgChooseCheckDate.setCheckOut(update, Integer.parseInt(data.replaceAll(RAA_SET_DAY, "")));
                update.getCallbackQuery().setData(RAA_CHOOSE_AN_APARTMENT);
                userMsgChooseAnApartment.addUserToSelector(update);
                messages.addAll(userMsgChooseAnApartment.sendPhotos(update,
                        "img/apartments/" + userMsgChooseAnApartment.getCurrentApartment(update).toString()));
                messages.add(userMsgChooseAnApartment.sendMessage(update));
            }
        } else {
            switch (data) {
                case RAA_SET_JANUARY ->
                        userMsgChooseCheckDate.setSelectedMonth(update, UserMsgChangeCheckInMonth.JANUARY);
                case RAA_SET_FEBRUARY ->
                        userMsgChooseCheckDate.setSelectedMonth(update, UserMsgChangeCheckInMonth.FEBRUARY);
                case RAA_SET_MARCH ->
                        userMsgChooseCheckDate.setSelectedMonth(update, UserMsgChangeCheckInMonth.MARCH);
                case RAA_SET_APRIL ->
                        userMsgChooseCheckDate.setSelectedMonth(update, UserMsgChangeCheckInMonth.APRIL);
                case RAA_SET_MAY -> userMsgChooseCheckDate.setSelectedMonth(update, UserMsgChangeCheckInMonth.MAY);
                case RAA_SET_JUNE -> userMsgChooseCheckDate.setSelectedMonth(update, UserMsgChangeCheckInMonth.JUNE);
                case RAA_SET_JULY -> userMsgChooseCheckDate.setSelectedMonth(update, UserMsgChangeCheckInMonth.JULY);
                case RAA_SET_AUGUST ->
                        userMsgChooseCheckDate.setSelectedMonth(update, UserMsgChangeCheckInMonth.AUGUST);
                case RAA_SET_SEPTEMBER ->
                        userMsgChooseCheckDate.setSelectedMonth(update, UserMsgChangeCheckInMonth.SEPTEMBER);
                case RAA_SET_OCTOBER ->
                        userMsgChooseCheckDate.setSelectedMonth(update, UserMsgChangeCheckInMonth.OCTOBER);
                case RAA_SET_NOVEMBER ->
                        userMsgChooseCheckDate.setSelectedMonth(update, UserMsgChangeCheckInMonth.NOVEMBER);
                case RAA_SET_DECEMBER ->
                        userMsgChooseCheckDate.setSelectedMonth(update, UserMsgChangeCheckInMonth.DECEMBER);
                default -> log.warn("Unknown RAA_SET data: {}", data);
            }
            processingRAA_SET_sendMessage(update, messages);
        }
    }

    private void processingRAA_SET_sendMessage(Update update, List<Message> messages) throws TelegramApiException {
        update.getCallbackQuery().setData(RAA_CHOOSE_CHECK_DATE);
        messages.add(userMsgChooseCheckDate.sendMessage(update));
    }
}
