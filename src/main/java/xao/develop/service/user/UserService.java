package xao.develop.service.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import xao.develop.config.GeneralCommand;
import xao.develop.config.GeneralMessageLink;
import xao.develop.config.UserCommand;
import xao.develop.config.UserMessageLink;
import xao.develop.config.enums.Card;
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

        String data = service.getData(update);

        log.debug("Data: {}", data);

        List<Integer> messages = new ArrayList<>();

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

            for (Integer message : messages)
                service.registerMessage(service.getChatId(update), message);
        } catch (TelegramApiException ex) {
            log.error("execute: {}", ex.getMessage());
        }

        log.trace("Method execute(Update, String) finished");
    }

    private void processingMessage(Update update,
                                   List<Integer> messages,
                                   String data,
                                   String[] parameters) throws TelegramApiException {

        if (parameters.length == 2 && service.getChatId(update).longValue() == userMsgChooseAnApartment.getUserId(update)) {
            long chatId = service.getChatId(update);
            int msgId = service.getMessageId(update);

            switch (data) {
                case CARD_NAME -> cardDataProcessing(update, messages, chatId, msgId, parameters[1], Card.NAME);
                case CARD_SURNAME -> cardDataProcessing(update, messages, chatId, msgId, parameters[1], Card.SURNAME);
                case CARD_GENDER -> cardDataProcessing(update, messages, chatId, msgId, parameters[1], Card.GENDER);
                case CARD_AGE -> cardDataProcessing(update, messages, chatId, msgId, parameters[1], Card.AGE);
                case CARD_COUNT -> cardDataProcessing(update, messages, chatId, msgId, parameters[1], Card.COUNT);
                case CARD_CONTACTS -> cardDataProcessing(update, messages, chatId, msgId, parameters[1], Card.CONTACTS);
            }
        } else {
            if (data.equals(START)) {
                service.authorization(update.getMessage());
                userMsgStart.editMessage(update, messages, USER_MSG_START);
            } else
                log.info("Unknown message data: {}", data);

            service.deleteLastMessage(service.getChatId(update), service.getMessageId(update));
        }
    }

    private void processingCallbackQueryRAA(Update update, List<Integer> messages, String data) throws TelegramApiException {
        if (data.startsWith(RAA + SET)) {
            processingRAA_SET(update, messages, data);
        } else
            switch (data) {
                case RAA_CHOOSE_CHECK_DATE -> {
                    if (userMsgChooseCheckDate.checkIsAlreadyExistRent(update)) {
                        userInfoMessage.legacyEditMessage(update,
                                messages,
                                USER_MSG_ALREADY_EXIST_RENT,
                                GENERAL_BT_BACK,
                                BACK_TO_START);
                    } else {
                        userMsgChooseCheckDate.addNewUserToTempBookingData(update);
                        userMsgChooseCheckDate.editMessage(update, messages, USER_MSG_CHOOSE_CHECK_IN_DATE);
                    }
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
                        update.getCallbackQuery().setData(RAA_CHOOSE_CHECK_DATE); // todo Можно убрать по идее
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
                    update.getCallbackQuery().setData(RAA_CHOOSE_CHECK_OUT_DATE); // todo Можно убрать по идее
                    userMsgChooseCheckDate.editMessage(update, messages, USER_MSG_CHOOSE_CHECK_OUT_DATE);
                }
                case RAA_BOOK -> {
                    if (!userMsgChooseAnApartment.getIsBooking(update)) {
                        userMsgChooseAnApartment.setIsBooking(update, true);
                        initMsgBooking(update, messages, USER_MSG_SET_NAME);
                    } else
                        userInfoMessage.editMessage(update, messages, USER_MSG_CAN_NOT_NOOK);
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
                case RAA_SHOW_PREVIEW -> userMsgPreviewCard.editMessage(update, messages, USER_MSG_SHOW_PREVIEW,
                            userMsgPreviewCard.getPackParameters(update));
                case RAA_QUIT_FROM_PREVIEW_CARD -> initMsgBooking(update, messages, USER_MSG_BOOK);
                case RAA_SEND_BOOKING_TO_ADMIN -> {
                    update.getCallbackQuery().setData(START);
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

    private void processingCallbackQuery(Update update, List<Integer> messages, String data) throws TelegramApiException {
        switch (data) {
            case ABOUT_US -> userMsgAboutUs.editMessage(update, messages, USER_MSG_ABOUT_US);
            case CONTACTS -> userMsgContacts.editMessage(update, messages, USER_MSG_CONTACTS, service.getAdminContacts());
            case CHANGE_LANGUAGE -> userMsgChangeLanguage.editMessage(update, messages, USER_MSG_CHANGE_LANGUAGE);
            case BACK_TO_START -> {
                update.getCallbackQuery().setData(START);
                userMsgStart.editMessage(update, messages, USER_MSG_START);
            }
            case EN, TR, RU -> {
                service.setLanguage(update, data);
                update.getCallbackQuery().setData(START);
                userMsgStart.editMessage(update, messages, USER_MSG_START);
            }
            case DELETE -> service.deleteMessage(service.getChatId(update),
                    update.getCallbackQuery().getMessage().getMessageId());
            default -> log.info("Unknown callback query data: {}", data);
        }
    }

    private void processingRAA_SET(Update update, List<Integer> messages, String data) throws TelegramApiException {
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

    private void cardDataProcessing(Update update,
                                       List<Integer> messages,
                                       long chatId,
                                       int msgId,
                                       String data,
                                       Card type) throws TelegramApiException {
        switch (type) {
            case NAME -> {
                if (data.matches("[a-zA-Z]+"))
                    correctInputCard(update, messages, chatId, msgId, data, type);
                else
                    initIncorrectEnterCard(chatId, msgId, USER_MSG_SIMPLE_INCORRECT_NAME);
            }
            case SURNAME -> {
                if (data.matches("[a-zA-Z]+"))
                    correctInputCard(update, messages, chatId, msgId, data, type);
                else
                    initIncorrectEnterCard(chatId, msgId, USER_MSG_SIMPLE_INCORRECT_SURNAME);
            }
            case GENDER -> {
                if (data.matches("[MW]"))
                    correctInputCard(update, messages, chatId, msgId, data, type);
                else
                    initIncorrectEnterCard(chatId, msgId, USER_MSG_SIMPLE_INCORRECT_GENDER);
            }
            case AGE -> {
                if (data.matches("[0-9]+") &&
                        Integer.parseInt(data) >= 18 &&
                        Integer.parseInt(data) <= 95)
                    correctInputCard(update, messages, chatId, msgId, data, type);
                else
                    initIncorrectEnterCard(chatId, msgId, USER_MSG_SIMPLE_INCORRECT_AGE);
            }
            case COUNT -> {
                if (data.matches("[0-9]+") &&
                        Integer.parseInt(data) >= 1 &&
                        Integer.parseInt(data) <= 5)
                    correctInputCard(update, messages, chatId, msgId, data, type);
                else
                    initIncorrectEnterCard(chatId, msgId, USER_MSG_SIMPLE_INCORRECT_COUNT);
            }
            case CONTACTS -> correctInputCard(update, messages, chatId, msgId, data, type);
        }
    }

    private void correctInputCard(Update update,
                                  List<Integer> messages,
                                  long chatId,
                                  int msgId,
                                  String data,
                                  Card type) throws TelegramApiException {

        service.deleteLastMessage(chatId, msgId);

        switch (type) {
            case NAME -> {
                userMsgBooking.setName(chatId, data);
                initMsgBooking(update, messages, USER_MSG_SET_SURNAME);
            }
            case SURNAME -> {
                userMsgBooking.setSurname(chatId, data);
                initMsgBooking(update, messages, USER_MSG_SET_GENDER);
            }
            case GENDER -> {
                userMsgBooking.setGender(chatId, data);
                initMsgBooking(update, messages, USER_MSG_SET_AGE);
            }
            case AGE -> {
                userMsgBooking.setAge(chatId, data);
                initMsgBooking(update, messages, USER_MSG_SET_COUNT);
            }
            case COUNT -> {
                userMsgBooking.setCount(chatId, data);
                initMsgBooking(update, messages, USER_MSG_SET_CONTACTS);
            }
            case CONTACTS -> {
                userMsgBooking.setContacts(chatId, data);
                initMsgBooking(update, messages, USER_MSG_BOOK);
            }
        }
    }

    private void initMsgChooseCheckDate(Update update, List<Integer> messages) throws TelegramApiException {
        update.getCallbackQuery().setData(RAA_CHOOSE_CHECK_DATE);

        String message = userMsgChooseCheckDate.isCheckInSet(update) ?
                USER_MSG_CHOOSE_CHECK_OUT_DATE : USER_MSG_CHOOSE_CHECK_IN_DATE;

        userMsgChooseCheckDate.editMessage(update, messages, message);
    }

    private void initMsgApartments(Update update, List<Integer> messages) throws TelegramApiException {
        int msgId = service.sendSimpleMessage(service.getChatId(update), USER_MSG_SIMPLE_DOWNLOADING);

        update.getCallbackQuery().setData(RAA_CHOOSE_AN_APARTMENT);
        userMsgChooseAnApartment.sendPhotos(update, messages,
                "img/apartments/" + userMsgChooseAnApartment.getCurrentApartment(update).toString());
        userMsgChooseAnApartment.sendMessage(update, messages);

        service.deleteLastMessage(service.getChatId(update), msgId);
    }

    private void initMsgBooking(Update update, List<Integer> messages, String msgLink) throws TelegramApiException {
        userMsgBooking.editMessage(update, messages, msgLink, userMsgBooking.getTempBookingData(update));
    }

    private boolean initIncorrectEnterCard(long chatId, int msgId, String msgLink) {
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
        return false;
    }
}
