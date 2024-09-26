package xao.develop.server.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import xao.develop.config.UserCommand;
import xao.develop.model.TempBotMessage;
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
    UserMsgBooking userMsgBooking;

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

        String[] parameters = data.split(" ");
        data = parameters[0];

        try {
            if (data.startsWith(RAA)) {
                processingRAA(update, messages, data);
            } else
                switch (data) {
                    case START -> {
                        server.authorization(update.getMessage());
                        messages.add(userMsgStart.sendMessage(update));
                    }
                    case CARD_NAME -> {
                        if (parameters.length == 2 &&
                                server.getChatId(update).longValue() == userMsgChooseAnApartment.getUserId(update) &&
                                parameters[1].matches("[a-zA-Z]+")) {
                            server.deleteLastMessage(update);
                            userMsgBooking.setName(update, parameters[1]);
                            update.getMessage().setText(RAA_BOOK);
                            messages.add(userMsgBooking.sendMessage(update));
                        } else
                            server.deleteLastMessage(update);
                    }
                    case CARD_SURNAME -> {
                        if (parameters.length == 2 &&
                                server.getChatId(update).longValue() == userMsgChooseAnApartment.getUserId(update) &&
                                parameters[1].matches("[a-zA-Z]+")) {
                            server.deleteLastMessage(update);
                            userMsgBooking.setSurname(update, parameters[1]);
                            update.getMessage().setText(RAA_BOOK);
                            messages.add(userMsgBooking.sendMessage(update));
                        } else
                            server.deleteLastMessage(update);
                    }
                    case CARD_GENDER -> {
                        if (parameters.length == 2 &&
                                server.getChatId(update).longValue() == userMsgChooseAnApartment.getUserId(update) &&
                                parameters[1].matches("[MW]")) {
                            server.deleteLastMessage(update);
                            userMsgBooking.setGender(update, parameters[1]);
                            update.getMessage().setText(RAA_BOOK);
                            messages.add(userMsgBooking.sendMessage(update));
                        } else
                            server.deleteLastMessage(update);
                    }
                    case CARD_AGE -> {
                        if (parameters.length == 2 &&
                                server.getChatId(update).longValue() == userMsgChooseAnApartment.getUserId(update) &&
                                parameters[1].matches("[0-9]+") &&
                                Integer.parseInt(parameters[1]) < 150) {
                            server.deleteLastMessage(update);
                            userMsgBooking.setAge(update, parameters[1]);
                            update.getMessage().setText(RAA_BOOK);
                            messages.add(userMsgBooking.sendMessage(update));
                        } else
                            server.deleteLastMessage(update);
                    }
                    case CARD_COUNT -> {
                        if (parameters.length == 2 &&
                                server.getChatId(update).longValue() == userMsgChooseAnApartment.getUserId(update) &&
                                parameters[1].matches("[0-9]+")) {
                            server.deleteLastMessage(update);
                            userMsgBooking.setCount(update, parameters[1]);
                            update.getMessage().setText(RAA_BOOK);
                            messages.add(userMsgBooking.sendMessage(update));
                        } else
                            server.deleteLastMessage(update);
                    }
                    case CARD_CONTACTS -> {
                        if (parameters.length == 2 &&
                                server.getChatId(update).longValue() == userMsgChooseAnApartment.getUserId(update)) {
                            server.deleteLastMessage(update);
                            userMsgBooking.setContacts(update, parameters[1]);
                            update.getMessage().setText(RAA_BOOK);
                            messages.add(userMsgBooking.sendMessage(update));
                        } else
                            server.deleteLastMessage(update);
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
                    default -> {
                        if (update.hasMessage())
                            server.deleteLastMessage(update);
                        log.info("Unknown data: {}", data);
                    }
                }

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
                    sendApartment(update, messages);
                }
                case RAA_PREVIOUS_APARTMENT -> {
                    userMsgChooseAnApartment.downSelector(update);
                    sendApartment(update, messages);
                }
                case RAA_QUIT_FROM_CHOOSER_AN_APARTMENT -> {
                    userMsgChooseAnApartment.deleteTempApartmentSelector(update);
                    userMsgChooseCheckDate.deleteCheckOut(update);
                    update.getCallbackQuery().setData(RAA_CHOOSE_CHECK_OUT_DATE);
                    messages.add(userMsgChooseCheckDate.sendMessage(update));
                }
                case RAA_BOOK -> {
                    if (!userMsgChooseAnApartment.getIsBooking(update)) {
                        userMsgChooseAnApartment.setIsBooking(update, true);
                        userMsgBooking.createNewUserCard(update);
                        messages.add(userMsgBooking.sendMessage(update));
                    } else
                        messages.add(userMsgBooking.sendCanNotBook(update));
                }
                case RAA_QUIT_FROM_BOOKING_AN_APARTMENT -> {
                    userMsgChooseAnApartment.setIsBooking(update, false);
                    userMsgChooseAnApartment.deleteTempApartmentSelector(update);
                    userMsgChooseAnApartment.addTempApartmentSelector(update);
                    sendApartment(update, messages);
                }
                case RAA_QUIT_CAN_NOT_BOOK -> {
                    userMsgChooseAnApartment.deleteTempApartmentSelector(update);
                    userMsgChooseAnApartment.addTempApartmentSelector(update);
                    sendApartment(update, messages);
                }
                default -> log.info("Unknown RAA data: {}", data);
            }
    }

    private void sendApartment(Update update, List<Message> messages) throws TelegramApiException {
        update.getCallbackQuery().setData(RAA_CHOOSE_AN_APARTMENT);
        messages.addAll(userMsgChooseAnApartment.sendPhotos(update,
                "img/apartments/" + userMsgChooseAnApartment.getCurrentApartment(update).toString()));
        messages.add(userMsgChooseAnApartment.sendMessage(update));
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
                userMsgChooseAnApartment.addTempApartmentSelector(update);
                sendApartment(update, messages);
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
