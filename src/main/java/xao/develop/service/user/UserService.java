package xao.develop.service.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import xao.develop.config.GeneralCommand;
import xao.develop.config.GeneralMessageLink;
import xao.develop.config.UserCommand;
import xao.develop.config.UserMessageLink;
import xao.develop.config.enums.Card;
import xao.develop.config.enums.Month;
import xao.develop.config.enums.Selector;
import xao.develop.service.BotService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class UserService implements GeneralMessageLink, GeneralCommand, UserCommand, UserMessageLink {

    @Autowired
    BotService service;

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
    UserInfoMessage userInfoMessage;
    @Autowired
    UserMsgAlreadyRenting userMsgAlreadyRenting;
    @Autowired
    UserMsgNoFreeApartment userMsgNoFreeApartment;
    @Autowired
    UserMsgPreviewCard userMsgPreviewCard;

    @Autowired
    UserMsgAboutUs userMsgAboutUs;

    @Autowired
    UserMsgContacts userMsgContacts;

    @Autowired
    UserMsgChangeLanguage userMsgChangeLanguage;

    private final ScheduledExecutorService scheduled = Executors.newScheduledThreadPool(1);

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
            else if (update.hasCallbackQuery() && data[0].startsWith(RAA))
                processingCallbackQueryRAA(update, messages, data[0]);
            else if (update.hasCallbackQuery())
                processingCallbackQuery(chatId, msgId, user, messages, data[0]);
            else
                log.info("Unknown data: {}", data[0]);

            log.debug("Is the list of messages empty? {}", messages.isEmpty());

            for (Integer message : messages)
                service.registerMessage(service.getChatId(update), message);
        } catch (TelegramApiException ex) {
            log.error("execute: {}", ex.getMessage());
        }

        log.trace("Method execute(Update, String) finished");
    }

    private void processingMessage(long chatId,
                                   int msgId,
                                   User user,
                                   List<Integer> messages,
                                   String[] data) throws TelegramApiException {

        if (data.length == 2 && chatId == userMsgStart.getBookingUserIdApartment(chatId)) {
            switch (data[0]) {
                case CARD_NAME -> cardDataProcessing(chatId, msgId, messages, data[1], Card.NAME);
                case CARD_SURNAME -> cardDataProcessing(chatId, msgId, messages, data[1], Card.SURNAME);
                case CARD_GENDER -> cardDataProcessing(chatId, msgId, messages, data[1], Card.GENDER);
                case CARD_AGE -> cardDataProcessing(chatId, msgId, messages, data[1], Card.AGE);
                case CARD_COUNT -> cardDataProcessing(chatId, msgId, messages, data[1], Card.COUNT);
                case CARD_CONTACTS -> cardDataProcessing(chatId, msgId, messages, data[1], Card.CONTACTS);
            }
        } else {
            if (data[0].equals(START)) {
                start(chatId, user, messages, true);
            } else
                log.info("Unknown message data: {}", data[0]);

            service.deleteLastMessage(chatId, msgId);
        }
    }

    private void processingCallbackQueryRAA(Update update, List<Integer> messages, String data) throws TelegramApiException {
        if (data.startsWith(RAA + SET)) {
            processingRAA_SET(update, messages, data);
        } else
            switch (data) {
                case RAA_CHANGE_CHECK_YEAR -> userMsgChangeCheckYear.editMessage(update, messages,
                        USER_MSG_CHANGE_CHECK_YEAR);
                case RAA_CHANGE_CHECK_MONTH -> userMsgChangeCheckMonth.editMessage(update, messages,
                        USER_MSG_CHANGE_CHECK_MONTH);
                case RAA_NEXT_CHECK_YEAR_CM -> {
                    userMsgChooseCheckDate.nextYear(service.getChatId(update));
                    userMsgChangeCheckMonth.editMessage(update, messages, USER_MSG_CHANGE_CHECK_MONTH);
                }
                case RAA_PREVIOUS_CHECK_YEAR_CM -> {
                    userMsgChooseCheckDate.previousYear(update);
                    userMsgChangeCheckMonth.editMessage(update, messages, USER_MSG_CHANGE_CHECK_MONTH);
                }
                case RAA_QUIT_FROM_CHANGE_CHECK_MONTH -> initMsgChooseCheckDate(update, messages);
                case RAA_NEXT_CHECK_YEAR -> {
                    userMsgChooseCheckDate.nextYear(service.getChatId(update));
                    initMsgChooseCheckDate(update, messages);
                }
                case RAA_PREVIOUS_CHECK_YEAR -> {
                    userMsgChooseCheckDate.previousYear(update);
                    initMsgChooseCheckDate(update, messages);
                }
                case RAA_NEXT_CHECK_MONTH -> {
                    userMsgChooseCheckDate.nextMonth(service.getChatId(update));
                    initMsgChooseCheckDate(update, messages);
                }
                case RAA_PREVIOUS_CHECK_MONTH -> {
                    userMsgChooseCheckDate.previousMonth(service.getChatId(update));
                    initMsgChooseCheckDate(update, messages);
                }
                case RAA_QUIT_FROM_CHOOSER_CHECK -> {
                    if (userMsgChooseCheckDate.isCheckInSet(service.getChatId(update))) {
                        userMsgChooseCheckDate.deleteCheckIn(service.getChatId(update));
                        userMsgChooseCheckDate.editMessage(update, messages, USER_MSG_CHOOSE_CHECK_IN_DATE);
                    } else {
                        userMsgChooseCheckDate.deleteUserFromTempBookingData(update);
                        messages.add(userMsgStart.editMessage(service.getChatId(update), USER_MSG_START));
                    }
                }
                case RAA_NEXT_APARTMENT -> {
                    userMsgChooseAnApartment.changeSelector(service.getChatId(update), Selector.NEXT);
                    initMsgApartments(service.getChatId(update), messages);
                }
                case RAA_PREVIOUS_APARTMENT -> {
                    userMsgChooseAnApartment.changeSelector(service.getChatId(update), Selector.PREVIOUS);
                    initMsgApartments(service.getChatId(update), messages);
                }
                case RAA_QUIT_FROM_CHOOSER_AN_APARTMENT -> {
                    userMsgChooseAnApartment.deleteTempApartmentSelector(service.getChatId(update));
                    userMsgChooseCheckDate.deleteCheckOut(update);
                    userMsgChooseCheckDate.editMessage(update, messages, USER_MSG_CHOOSE_CHECK_OUT_DATE);
                }
                case RAA_BOOK -> {
                    if (!userMsgChooseAnApartment.isBookingApartment(service.getChatId(update))) {
                        userMsgChooseAnApartment.setIsBookingApartment(service.getChatId(update), true);
                        initMsgBooking(service.getChatId(update), messages, USER_MSG_SET_NAME);
                    } else
                        userInfoMessage.editMessage(update, messages, USER_MSG_CAN_NOT_NOOK);
                }
                case RAA_QUIT_FROM_BOOKING_AN_APARTMENT -> {
                    userMsgChooseAnApartment.setIsBookingApartment(service.getChatId(update), false);
                    userMsgChooseAnApartment.deleteTempApartmentSelector(service.getChatId(update));
                    userMsgChooseAnApartment.addTempApartmentSelector(service.getChatId(update));
                    initMsgApartments(service.getChatId(update), messages);
                }
                case RAA_QUIT_CAN_NOT_BOOK -> {
                    userMsgChooseAnApartment.deleteTempApartmentSelector(service.getChatId(update));
                    userMsgChooseAnApartment.addTempApartmentSelector(service.getChatId(update));
                    initMsgApartments(service.getChatId(update), messages);
                }
                case RAA_SHOW_PREVIEW -> userMsgPreviewCard.editMessage(update, messages, USER_MSG_SHOW_PREVIEW,
                            userMsgPreviewCard.getPackParameters(update));
                case RAA_QUIT_FROM_PREVIEW_CARD -> initMsgBooking(service.getChatId(update), messages, USER_MSG_BOOK);
                case RAA_SEND_BOOKING_TO_ADMIN -> {
                    userMsgStart.editMessage(update, messages, USER_MSG_START);
                    service.sendMessageAdminUserUpdatedStatus(service.getAdminId(), GENERAL_MSG_GOT_NEW_APP,
                            userMsgStart.getIKMarkupGotNewApp(service.getAdminId()));
                    int msgId = service.sendSimpleMessage(service.getChatId(update), USER_MSG_SIMPLE_SEND_MESSAGE_TO_ADMIN);
                    scheduled.schedule(() ->
                            service.deleteLastMessage(service.getChatId(update), msgId), 10, TimeUnit.SECONDS);
                }

                default -> log.info("Unknown RAA data: {}", data);
            }
    }

    private void processingCallbackQuery(long chatId,
                                         int msgId,
                                         User user,
                                         List<Integer> messages,
                                         String data) throws TelegramApiException {
        switch (data) {
            case ABOUT_US -> openAboutUs(chatId, messages);
            case CONTACTS -> openContacts(chatId, messages);
            case CHANGE_LANGUAGE -> openChangeLanguage(chatId, messages);
            case BACK_TO_START -> start(chatId, user, messages, false);
            case EN, TR, RU -> changeLanguage(chatId, user, messages, data);
            case CHOOSE_CHECK_DATE -> chooseCheckDate(chatId, user, messages);
            case DELETE -> deleteMessage(chatId, msgId);
            default -> log.info("Unknown callback query data: {}", data);
        }
    }

    private void processingRAA_SET(Update update, List<Integer> messages, String data) throws TelegramApiException {
        if (data.startsWith(RAA_SET_YEAR)) {
            userMsgChangeCheckYear.setYear(
                    service.getChatId(update), Integer.parseInt(data.replaceAll(RAA_SET_YEAR, "")));
            initMsgChooseCheckDate(update, messages);
        } else if (data.startsWith(RAA_SET_DAY)) {
            if (!userMsgChooseCheckDate.isCheckInSet(service.getChatId(update))) {
                userMsgChooseCheckDate.setCheckIn(
                        service.getChatId(update), Integer.parseInt(data.replaceAll(RAA_SET_DAY, "")));
                userMsgChooseCheckDate.editMessage(update, messages, USER_MSG_CHOOSE_CHECK_OUT_DATE);
            } else {
                userMsgChooseCheckDate.setCheckOut(
                        service.getChatId(update), Integer.parseInt(data.replaceAll(RAA_SET_DAY, "")));
                userMsgChooseAnApartment.addTempApartmentSelector(service.getChatId(update));
                initMsgApartments(service.getChatId(update), messages);
            }
        } else {
            switch (data) {
                case RAA_SET_JANUARY -> userMsgChooseCheckDate.setSelectedMonth(update, Month.JANUARY.getMonth());
                case RAA_SET_FEBRUARY -> userMsgChooseCheckDate.setSelectedMonth(update, Month.FEBRUARY.getMonth());
                case RAA_SET_MARCH -> userMsgChooseCheckDate.setSelectedMonth(update, Month.MARCH.getMonth());
                case RAA_SET_APRIL -> userMsgChooseCheckDate.setSelectedMonth(update, Month.APRIL.getMonth());
                case RAA_SET_MAY -> userMsgChooseCheckDate.setSelectedMonth(update, Month.MAY.getMonth());
                case RAA_SET_JUNE -> userMsgChooseCheckDate.setSelectedMonth(update, Month.JUNE.getMonth());
                case RAA_SET_JULY -> userMsgChooseCheckDate.setSelectedMonth(update, Month.JULY.getMonth());
                case RAA_SET_AUGUST -> userMsgChooseCheckDate.setSelectedMonth(update, Month.AUGUST.getMonth());
                case RAA_SET_SEPTEMBER -> userMsgChooseCheckDate.setSelectedMonth(update, Month.SEPTEMBER.getMonth());
                case RAA_SET_OCTOBER -> userMsgChooseCheckDate.setSelectedMonth(update, Month.OCTOBER.getMonth());
                case RAA_SET_NOVEMBER -> userMsgChooseCheckDate.setSelectedMonth(update, Month.NOVEMBER.getMonth());
                case RAA_SET_DECEMBER -> userMsgChooseCheckDate.setSelectedMonth(update, Month.DECEMBER.getMonth());
                default -> log.warn("Unknown RAA_SET data: {}", data);
            }
            initMsgChooseCheckDate(update, messages);
        }
    }

    private void openAboutUs(long chatId, List<Integer> messages) throws TelegramApiException {
        messages.add(userMsgAboutUs.editMessage(chatId, USER_MSG_ABOUT_US));
    }

    private void openContacts(long chatId, List<Integer> messages) throws TelegramApiException {
        messages.add(userMsgContacts.editMessage(chatId, USER_MSG_CONTACTS, service.getAdminContacts()));
    }

    private void openChangeLanguage(long chatId, List<Integer> messages) throws TelegramApiException {
        messages.add(userMsgChangeLanguage.editMessage(chatId, USER_MSG_CHANGE_LANGUAGE));
    }

    private void cardDataProcessing(long chatId,
                                    int msgId,
                                    List<Integer> messages,
                                    String data,
                                    Card type) throws TelegramApiException {
        switch (type) {
            case NAME -> {
                if (data.matches("[a-zA-Z]+"))
                    correctInputCard(chatId, msgId, messages, data, type);
                else
                    initIncorrectEnterCard(chatId, msgId, USER_MSG_SIMPLE_INCORRECT_NAME);
            }
            case SURNAME -> {
                if (data.matches("[a-zA-Z]+"))
                    correctInputCard(chatId, msgId, messages, data, type);
                else
                    initIncorrectEnterCard(chatId, msgId, USER_MSG_SIMPLE_INCORRECT_SURNAME);
            }
            case GENDER -> {
                if (data.matches("[MW]"))
                    correctInputCard(chatId, msgId, messages, data, type);
                else
                    initIncorrectEnterCard(chatId, msgId, USER_MSG_SIMPLE_INCORRECT_GENDER);
            }
            case AGE -> {
                if (data.matches("[0-9]+") &&
                        Integer.parseInt(data) >= 18 &&
                        Integer.parseInt(data) <= 95)
                    correctInputCard(chatId, msgId, messages, data, type);
                else
                    initIncorrectEnterCard(chatId, msgId, USER_MSG_SIMPLE_INCORRECT_AGE);
            }
            case COUNT -> {
                if (data.matches("[0-9]+") &&
                        Integer.parseInt(data) >= 1 &&
                        Integer.parseInt(data) <= 5)
                    correctInputCard(chatId, msgId, messages, data, type);
                else
                    initIncorrectEnterCard(chatId, msgId, USER_MSG_SIMPLE_INCORRECT_COUNT);
            }
            case CONTACTS -> correctInputCard(chatId, msgId, messages, data, type);
        }
    }

    private void correctInputCard(long chatId,
                                  int msgId,
                                  List<Integer> messages,
                                  String data,
                                  Card type) throws TelegramApiException {

        service.deleteLastMessage(chatId, msgId);

        switch (type) {
            case NAME -> {
                userMsgBooking.setName(chatId, data);
                initMsgBooking(chatId, messages, USER_MSG_SET_SURNAME);
            }
            case SURNAME -> {
                userMsgBooking.setSurname(chatId, data);
                initMsgBooking(chatId, messages, USER_MSG_SET_GENDER);
            }
            case GENDER -> {
                userMsgBooking.setGender(chatId, data);
                initMsgBooking(chatId, messages, USER_MSG_SET_AGE);
            }
            case AGE -> {
                userMsgBooking.setAge(chatId, data);
                initMsgBooking(chatId, messages, USER_MSG_SET_COUNT);
            }
            case COUNT -> {
                userMsgBooking.setCount(chatId, data);
                initMsgBooking(chatId, messages, USER_MSG_SET_CONTACTS);
            }
            case CONTACTS -> {
                userMsgBooking.setContacts(chatId, data);
                initMsgBooking(chatId, messages, USER_MSG_BOOK);
            }
        }
    }

    private void start(long chatId, User user, List<Integer> messages, boolean isInit) throws TelegramApiException {
        if (isInit)
            service.authorization(chatId, user);

        messages.add(userMsgStart.editMessage(chatId, USER_MSG_START));
    }

    private void changeLanguage(long chatId, User user, List<Integer> messages, String data) throws TelegramApiException {
        service.setLanguage(chatId, data);
        start(chatId, user, messages, false);
    }

    private void deleteMessage(long chatId, int msgId) {
        service.deleteMessage(chatId, msgId);
    }

    private void chooseCheckDate(long chatId, User user, List<Integer> messages) throws TelegramApiException {
        if (userMsgChooseCheckDate.checkIsAlreadyExistRent(chatId)) {
            messages.add(userMsgAlreadyRenting.editMessage(chatId, USER_MSG_ALREADY_EXIST_RENT));
        } else {
            userMsgChooseCheckDate.addNewUserToTempBookingData(chatId, user);
            messages.add(userMsgChooseCheckDate.editMessage(chatId, USER_MSG_CHOOSE_CHECK_IN_DATE));
        }
    }

    private void initMsgChooseCheckDate(Update update, List<Integer> messages) {
        String message = userMsgChooseCheckDate.isCheckInSet(service.getChatId(update)) ?
                USER_MSG_CHOOSE_CHECK_OUT_DATE : USER_MSG_CHOOSE_CHECK_IN_DATE;

        userMsgChooseCheckDate.editMessage(update, messages, message);
    }

    private void initMsgApartments(long chatId, List<Integer> messages) throws TelegramApiException {
        int msgId = service.sendSimpleMessage(chatId, USER_MSG_SIMPLE_DOWNLOADING);

        messages.addAll(userMsgChooseAnApartment.sendPhotos(chatId,
                "img/apartments/" + userMsgChooseAnApartment.getCurrentApartment(chatId)));

        if (!userMsgStart.isApartmentsEmpty(chatId))
            messages.add(userMsgChooseAnApartment.sendMessage(chatId, USER_MSG_CHOOSE_AN_APARTMENT,
                    userMsgStart.getApartmentParameters(chatId)));
        else
            messages.add(userMsgNoFreeApartment.sendMessage(chatId, USER_MSG_NO_FREE_APARTMENTS));

        service.deleteLastMessage(chatId, msgId);
    }

    private void initMsgBooking(long chatId, List<Integer> messages, String msgLink) throws TelegramApiException {
        messages.add(userMsgBooking.editMessage(chatId, msgLink, userMsgBooking.getTempBookingData(chatId)));
    }

    private void initIncorrectEnterCard(long chatId, int msgId, String msgLink) {
        try {
            int simpleMsgId = service.sendSimpleMessage(chatId, msgLink);
            scheduled.schedule(() -> {
                service.deleteLastMessage(chatId, msgId);
                service.deleteLastMessage(chatId, simpleMsgId);
                    }, 10, TimeUnit.SECONDS);
        } catch (TelegramApiException ex) {
            log.warn("""
                    sendSimpleMessage(Update, String) can't send msgLink {} for user {}
                    Exception: {}""", msgLink, chatId, ex.getMessage());
        }
    }
}
