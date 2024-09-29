package xao.develop.server.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import xao.develop.config.UserCommand;
import xao.develop.config.UserMessageLink;
import xao.develop.model.TempBookingData;
import xao.develop.server.Keyboard;
import xao.develop.server.Server;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class UserServer implements UserCommand, UserMessageLink {

    @Autowired
    UserMsgStart userMsgStart;

    @Autowired
    UserMsgChooseCheckDate userMsgChooseCheckDate;
    @Autowired
    UserMsgChangeCheckMonth userMsgChangeCheckMonth;
    @Autowired
    UserMsgChangeCheckYear userMsgChangeCheckYear;
    @Autowired
    UserMsgChooseAnApartment userMsgChooseAnApartment;
    @Autowired
    UserMsgBooking userMsgBooking;
    @Autowired
    UserMsgBookCanNot userMsgBookCanNot;
    @Autowired
    UserMsgPreviewCard userMsgPreviewCard;

    @Autowired
    UserMsgAboutUs userMsgAboutUs;

    @Autowired
    UserMsgContacts userMsgContacts;

    @Autowired
    UserMsgChangeLanguage userMsgChangeLanguage;

    @Autowired
    Server server;

    private final ScheduledExecutorService scheduled = Executors.newScheduledThreadPool(1);

    public void execute(Update update) {
        log.trace("Method execute(Update, String) started");

        String data = server.getData(update);

        log.debug("Data: {}", data);

        List<Message> messages = new ArrayList<>();

        String[] parameters = data != null ? data.split(" ", 2) : new String[]{"null"};
        data = parameters[0];

        try {
            if (update.hasMessage())
                processingMessage(update, messages, data, parameters);
            else if (update.hasCallbackQuery() && data.startsWith(RAA))
                processingCallbackQueryRAA(update, messages, data);
            else if (update.hasCallbackQuery())
                processingCallbackQuery(update, messages, data);
            else
                log.info("Unknown data: {}", data);

            log.debug("Is the list of messages empty? {}", messages.isEmpty());

            for (Message message : messages)
                server.registerMessage(server.getChatId(update), message.getMessageId());
        } catch (TelegramApiException ex) {
            log.error("execute: {}", ex.getMessage());
        }

        log.trace("Method execute(Update, String) finished");
    }

    private void processingMessage(Update update,
                                   List<Message> messages,
                                   String data,
                                   String[] parameters) throws TelegramApiException {

        if (parameters.length == 2 && server.getChatId(update).longValue() == userMsgChooseAnApartment.getUserId(update)) {
            boolean isCommandCorrect = true;

            switch (data) {
                case CARD_NAME -> {
                    if (parameters[1].matches("[a-zA-Z]+"))
                        userMsgBooking.setName(update, parameters[1]);
                    else
                        isCommandCorrect = initIncorrectEnterCard(update, USER_MSG_SIMPLE_INCORRECT_NAME);
                }
                case CARD_SURNAME -> {
                    if (parameters[1].matches("[a-zA-Z]+"))
                        userMsgBooking.setSurname(update, parameters[1]);
                    else
                        isCommandCorrect = initIncorrectEnterCard(update, USER_MSG_SIMPLE_INCORRECT_SURNAME);
                }
                case CARD_GENDER -> {
                    if (parameters[1].matches("[MW]"))
                        userMsgBooking.setGender(update, parameters[1]);
                    else
                        isCommandCorrect = initIncorrectEnterCard(update, USER_MSG_SIMPLE_INCORRECT_GENDER);
                }
                case CARD_AGE -> {
                    if (parameters[1].matches("[0-9]+") &&
                            Integer.parseInt(parameters[1]) >= 18 &&
                            Integer.parseInt(parameters[1]) <= 95)
                        userMsgBooking.setAge(update, parameters[1]);
                    else
                        isCommandCorrect = initIncorrectEnterCard(update, USER_MSG_SIMPLE_INCORRECT_AGE);
                }
                case CARD_COUNT -> {
                    if (parameters[1].matches("[0-9]+") &&
                            Integer.parseInt(parameters[1]) >= 1 &&
                            Integer.parseInt(parameters[1]) <= 5)
                        userMsgBooking.setCount(update, parameters[1]);
                    else
                        isCommandCorrect = initIncorrectEnterCard(update, USER_MSG_SIMPLE_INCORRECT_COUNT);
                }
                case CARD_CONTACTS -> userMsgBooking.setContacts(update, parameters[1]);
                default -> isCommandCorrect = false;
            }

            if (isCommandCorrect) {
                server.deleteLastMessage(server.getChatId(update), server.getMessageId(update));
                update.getMessage().setText(RAA_BOOK);
                initMsgBooking(update, messages);
            }
        } else {
            if (data.equals(START)) {
                server.authorization(update.getMessage());
                userMsgStart.editMessage(update, messages, USER_MSG_START);
            } else
                log.info("Unknown message data: {}", data);

            server.deleteLastMessage(server.getChatId(update), server.getMessageId(update));
        }
    }

    private void processingCallbackQueryRAA(Update update, List<Message> messages, String data) throws TelegramApiException {
        if (data.startsWith(RAA + SET)) {
            processingRAA_SET(update, messages, data);
        } else
            switch (data) {
                case RAA_CHOOSE_CHECK_DATE -> {
                    userMsgChooseCheckDate.addNewUserToTempBookingData(update);
                    userMsgChooseCheckDate.editMessage(update, messages, USER_MSG_CHOOSE_CHECK_IN_DATE);
                }
                case RAA_CHANGE_CHECK_YEAR -> userMsgChangeCheckYear.editMessage(update, messages,
                        USER_MSG_CHANGE_CHECK_YEAR);
                case RAA_CHANGE_CHECK_MONTH -> userMsgChangeCheckMonth.editMessage(update, messages,
                        USER_MSG_CHANGE_CHECK_MONTH);
                case RAA_NEXT_CHECK_YEAR_CM -> {
                    userMsgChooseCheckDate.nextYear(update);
                    update.getCallbackQuery().setData(RAA_CHANGE_CHECK_MONTH);
                    userMsgChangeCheckMonth.editMessage(update, messages, USER_MSG_CHANGE_CHECK_MONTH);
                }
                case RAA_PREVIOUS_CHECK_YEAR_CM -> {
                    userMsgChooseCheckDate.previousYear(update);
                    update.getCallbackQuery().setData(RAA_CHANGE_CHECK_MONTH);
                    userMsgChangeCheckMonth.editMessage(update, messages, USER_MSG_CHANGE_CHECK_MONTH);
                }
                case RAA_QUIT_FROM_CHANGE_CHECK_MONTH -> initMsgChooseCheckDate(update, messages);
                case RAA_NEXT_CHECK_YEAR -> {
                    userMsgChooseCheckDate.nextYear(update);
                    initMsgChooseCheckDate(update, messages);
                }
                case RAA_PREVIOUS_CHECK_YEAR -> {
                    userMsgChooseCheckDate.previousYear(update);
                    initMsgChooseCheckDate(update, messages);
                }
                case RAA_NEXT_CHECK_MONTH -> {
                    userMsgChooseCheckDate.nextMonth(update);
                    initMsgChooseCheckDate(update, messages);
                }
                case RAA_PREVIOUS_CHECK_MONTH -> {
                    userMsgChooseCheckDate.previousMonth(update);
                    initMsgChooseCheckDate(update, messages);
                }
                case RAA_QUIT_FROM_CHOOSER_CHECK -> {
                    if (userMsgChooseCheckDate.isCheckInSet(update)) {
                        userMsgChooseCheckDate.deleteCheckIn(update);
                        update.getCallbackQuery().setData(RAA_CHOOSE_CHECK_DATE);
                        userMsgChooseCheckDate.editMessage(update, messages, USER_MSG_CHOOSE_CHECK_IN_DATE);
                    } else {
                        userMsgChooseCheckDate.deleteUserFromTempBookingData(update);
                        update.getCallbackQuery().setData(START);
                        userMsgStart.editMessage(update, messages, USER_MSG_START);
                    }
                }
                case RAA_NEXT_APARTMENT -> {
                    userMsgChooseAnApartment.upSelector(update);
                    initMsgApartments(update, messages);
                }
                case RAA_PREVIOUS_APARTMENT -> {
                    userMsgChooseAnApartment.downSelector(update);
                    initMsgApartments(update, messages);
                }
                case RAA_QUIT_FROM_CHOOSER_AN_APARTMENT -> {
                    userMsgChooseAnApartment.deleteTempApartmentSelector(update);
                    userMsgChooseCheckDate.deleteCheckOut(update);
                    update.getCallbackQuery().setData(RAA_CHOOSE_CHECK_OUT_DATE);
                    userMsgChooseCheckDate.editMessage(update, messages, USER_MSG_CHOOSE_CHECK_OUT_DATE);
                }
                case RAA_BOOK -> {
                    if (!userMsgChooseAnApartment.getIsBooking(update)) {
                        userMsgChooseAnApartment.setIsBooking(update, true);
                        initMsgBooking(update, messages);
                    } else
                        userMsgBookCanNot.editMessage(update, messages, USER_MSG_CAN_NOT_NOOK);
                }
                case RAA_QUIT_FROM_BOOKING_AN_APARTMENT -> {
                    userMsgChooseAnApartment.setIsBooking(update, false);
                    userMsgChooseAnApartment.deleteTempApartmentSelector(update);
                    userMsgChooseAnApartment.addTempApartmentSelector(update);
                    initMsgApartments(update, messages);
                }
                case RAA_QUIT_CAN_NOT_BOOK -> {
                    userMsgChooseAnApartment.deleteTempApartmentSelector(update);
                    userMsgChooseAnApartment.addTempApartmentSelector(update);
                    initMsgApartments(update, messages);
                }
                case RAA_SHOW_PREVIEW -> {
                    TempBookingData tempBookingData = userMsgBooking.getTempBookingData(update);

                    userMsgPreviewCard.editMessage(update, messages, USER_MSG_SHOW_PREVIEW,
                            tempBookingData.getNumberOfApartment(),
                            userMsgPreviewCard.getCheckDate(tempBookingData.getCheckIn()),
                            userMsgPreviewCard.getCheckDate(tempBookingData.getCheckOut()),
                            tempBookingData.getFirstName(),
                            tempBookingData.getLastName(),
                            tempBookingData.getAge(),
                            tempBookingData.getGender(),
                            tempBookingData.getCountOfPeople(),
                            tempBookingData.getContacts(),
                            userMsgPreviewCard.getTotalRent(
                                    tempBookingData.getCheckIn(),
                                    tempBookingData.getCheckOut(),
                                    tempBookingData.getCountOfPeople()));
                }
                case RAA_QUIT_FROM_PREVIEW_CARD -> initMsgBooking(update, messages);
                case RAA_SEND_BOOKING_TO_ADMIN -> {
                    userMsgChooseAnApartment.setIsBooking(update, false);
                    update.getCallbackQuery().setData(START);
                    userMsgStart.editMessage(update, messages, USER_MSG_START);
                    server.sendMessageAdminUser(Keyboard.CONFIRM_BOOKING);
                    int msgId = server.sendSimpleMessage(update, USER_MSG_SIMPLE_SEND_MESSAGE_TO_ADMIN);
                    scheduled.schedule(() -> server.deleteLastMessage(server.getChatId(update), msgId), 10, TimeUnit.SECONDS);
                }

                default -> log.info("Unknown RAA data: {}", data);
            }
    }

    private void processingCallbackQuery(Update update, List<Message> messages, String data) throws TelegramApiException {
        switch (data) {
            case ABOUT_US -> userMsgAboutUs.editMessage(update, messages, USER_MSG_ABOUT_US);
            case CONTACTS -> userMsgContacts.editMessage(update, messages, USER_MSG_CONTACTS,
                    server.getAdminPhone(),
                    server.getAdminEmail());
            case CHANGE_LANGUAGE -> userMsgChangeLanguage.editMessage(update, messages, USER_MSG_CHANGE_LANGUAGE);
            case BACK_TO_START -> {
                update.getCallbackQuery().setData(START);
                userMsgStart.editMessage(update, messages, USER_MSG_START);
            }
            case EN, TR, RU -> {
                server.setLanguage(update, data);
                update.getCallbackQuery().setData(START);
                userMsgStart.editMessage(update, messages, USER_MSG_START);
            }
            default -> log.info("Unknown callback query data: {}", data);
        }
    }

    private void processingRAA_SET(Update update, List<Message> messages, String data) throws TelegramApiException {
        if (data.startsWith(RAA_SET_YEAR)) {
            userMsgChangeCheckYear.setYear(update, Integer.parseInt(data.replaceAll(RAA_SET_YEAR, "")));
            initMsgChooseCheckDate(update, messages);
        } else if (data.startsWith(RAA_SET_DAY)) {
            if (!userMsgChooseCheckDate.isCheckInSet(update)) {
                userMsgChooseCheckDate.setCheckIn(update, Integer.parseInt(data.replaceAll(RAA_SET_DAY, "")));
                update.getCallbackQuery().setData(RAA_CHOOSE_CHECK_OUT_DATE);
                userMsgChooseCheckDate.editMessage(update, messages, USER_MSG_CHOOSE_CHECK_OUT_DATE);
            } else {
                userMsgChooseCheckDate.setCheckOut(update, Integer.parseInt(data.replaceAll(RAA_SET_DAY, "")));
                userMsgChooseAnApartment.addTempApartmentSelector(update);
                initMsgApartments(update, messages);
            }
        } else {
            switch (data) {
                case RAA_SET_JANUARY ->
                        userMsgChooseCheckDate.setSelectedMonth(update, UserMsgChangeCheckMonth.JANUARY);
                case RAA_SET_FEBRUARY ->
                        userMsgChooseCheckDate.setSelectedMonth(update, UserMsgChangeCheckMonth.FEBRUARY);
                case RAA_SET_MARCH ->
                        userMsgChooseCheckDate.setSelectedMonth(update, UserMsgChangeCheckMonth.MARCH);
                case RAA_SET_APRIL ->
                        userMsgChooseCheckDate.setSelectedMonth(update, UserMsgChangeCheckMonth.APRIL);
                case RAA_SET_MAY -> userMsgChooseCheckDate.setSelectedMonth(update, UserMsgChangeCheckMonth.MAY);
                case RAA_SET_JUNE -> userMsgChooseCheckDate.setSelectedMonth(update, UserMsgChangeCheckMonth.JUNE);
                case RAA_SET_JULY -> userMsgChooseCheckDate.setSelectedMonth(update, UserMsgChangeCheckMonth.JULY);
                case RAA_SET_AUGUST ->
                        userMsgChooseCheckDate.setSelectedMonth(update, UserMsgChangeCheckMonth.AUGUST);
                case RAA_SET_SEPTEMBER ->
                        userMsgChooseCheckDate.setSelectedMonth(update, UserMsgChangeCheckMonth.SEPTEMBER);
                case RAA_SET_OCTOBER ->
                        userMsgChooseCheckDate.setSelectedMonth(update, UserMsgChangeCheckMonth.OCTOBER);
                case RAA_SET_NOVEMBER ->
                        userMsgChooseCheckDate.setSelectedMonth(update, UserMsgChangeCheckMonth.NOVEMBER);
                case RAA_SET_DECEMBER ->
                        userMsgChooseCheckDate.setSelectedMonth(update, UserMsgChangeCheckMonth.DECEMBER);
                default -> log.warn("Unknown RAA_SET data: {}", data);
            }
            initMsgChooseCheckDate(update, messages);
        }
    }

    private void initMsgChooseCheckDate(Update update, List<Message> messages) throws TelegramApiException {
        update.getCallbackQuery().setData(RAA_CHOOSE_CHECK_DATE);

        String message = userMsgChooseCheckDate.isCheckInSet(update) ?
                USER_MSG_CHOOSE_CHECK_OUT_DATE : USER_MSG_CHOOSE_CHECK_IN_DATE;

        userMsgChooseCheckDate.editMessage(update, messages, message);
    }

    private void initMsgApartments(Update update, List<Message> messages) throws TelegramApiException {
        int msgId = server.sendSimpleMessage(update, USER_MSG_SIMPLE_DOWNLOADING);

        update.getCallbackQuery().setData(RAA_CHOOSE_AN_APARTMENT);
        messages.addAll(userMsgChooseAnApartment.sendPhotos(update,
                "img/apartments/" + userMsgChooseAnApartment.getCurrentApartment(update).toString()));
        messages.add(userMsgChooseAnApartment.sendMessage(update));

        server.deleteLastMessage(server.getChatId(update), msgId);
    }

    private void initMsgBooking(Update update, List<Message> messages) throws TelegramApiException {
        TempBookingData tempBookingData = userMsgBooking.getTempBookingData(update);

        userMsgBooking.editMessage(update, messages, USER_MSG_BOOK,
                tempBookingData.getFirstName(),
                tempBookingData.getLastName(),
                tempBookingData.getGender(),
                tempBookingData.getAge(),
                tempBookingData.getCountOfPeople(),
                tempBookingData.getContacts());
    }

    private boolean initIncorrectEnterCard(Update update, String msgLink) {
        try {
            int msgId = server.sendSimpleMessage(update, msgLink);
            scheduled.schedule(() -> {
                server.deleteLastMessage(server.getChatId(update), server.getMessageId(update));
                server.deleteLastMessage(server.getChatId(update), msgId);
                    }, 10, TimeUnit.SECONDS);
        } catch (TelegramApiException ex) {
            log.warn("""
                    sendSimpleMessage(Update, String) can't send msgLink {} for user {}
                    Exception: {}""", msgLink, server.getChatId(update), ex.getMessage());
        }
        return false;
    }
}
