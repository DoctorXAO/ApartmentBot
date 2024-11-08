package xao.develop.service.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import xao.develop.command.GeneralCommand;
import xao.develop.command.GeneralMessageLink;
import xao.develop.command.UserCommand;
import xao.develop.command.UserMessageLink;
import xao.develop.enums.*;
import xao.develop.service.BotService;

import java.util.ArrayList;
import java.util.List;

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
    UserMsgCanNotBook userMsgCanNotBook;
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

    public void execute(Update update) {
        log.trace("Method execute(Update, String) started");

        String data = service.getData(update);

        log.debug("Data: {}", data);

        List<Integer> messages = new ArrayList<>();

        long chatId = service.getChatId(update);
        int msgId = service.getMessageId(update);
        User user = service.getUser(update);

        try {
            if (update.hasMessage())
                processingMessage(chatId, msgId, user, messages, data);
            else if (update.hasCallbackQuery() && data.startsWith(RAA))
                processingCallbackQueryRAA(chatId, user, messages, data);
            else if (update.hasCallbackQuery())
                processingCallbackQuery(chatId, msgId, user, messages, data);
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

    private void processingMessage(long chatId,
                                   int msgId,
                                   User user,
                                   List<Integer> messages,
                                   String data) throws TelegramApiException {

        if (data.equals(START)) {
            start(chatId, user, messages);
        } else if (!userMsgStart.getUserStep(chatId).equals(UserStep.EMPTY.getStep())) {
            enterPersonalData(chatId, msgId, messages, data);
        } else {
            deleteMessage(chatId, msgId);
            log.info("Unknown message data: {}", data);
        }
    }

    private void enterPersonalData(long chatId, int msgId, List<Integer> messages, String data) throws TelegramApiException {
        UserStep step = UserStep.valueOf(userMsgStart.getUserStep(chatId).toUpperCase());

        switch (data) {
            case CARD_NAME -> preInitMsgBooking(chatId, msgId, messages, UserStep.SET_NAME, USER_MSG_SET_NAME);
            case CARD_SURNAME -> preInitMsgBooking(chatId, msgId, messages, UserStep.SET_SURNAME, USER_MSG_SET_SURNAME);
            case CARD_GENDER -> preInitMsgBooking(chatId, msgId, messages, UserStep.SET_GENDER, USER_MSG_SET_GENDER);
            case CARD_AGE -> preInitMsgBooking(chatId, msgId, messages, UserStep.SET_AGE, USER_MSG_SET_AGE);
            case CARD_COUNT -> preInitMsgBooking(chatId, msgId, messages, UserStep.SET_COUNT, USER_MSG_SET_COUNT);
            case CARD_CONTACTS -> preInitMsgBooking(chatId, msgId, messages, UserStep.SET_CONTACTS, USER_MSG_SET_CONTACTS);
            default -> {
                switch (step) {
                    case SET_NAME -> cardDataProcessing(chatId, msgId, messages, data, Card.NAME);
                    case SET_SURNAME -> cardDataProcessing(chatId, msgId, messages, data, Card.SURNAME);
                    case SET_GENDER -> cardDataProcessing(chatId, msgId, messages, data, Card.GENDER);
                    case SET_AGE -> cardDataProcessing(chatId, msgId, messages, data, Card.AGE);
                    case SET_COUNT -> cardDataProcessing(chatId, msgId, messages, data, Card.COUNT);
                    case SET_CONTACTS -> cardDataProcessing(chatId, msgId, messages, data, Card.CONTACTS);
                    case COMPLETE -> deleteMessage(chatId, msgId);
                }
            }
        }
    }

    private void preInitMsgBooking(long chatId,
                                   int msgId,
                                   List<Integer> messages,
                                   UserStep step,
                                   String link) throws TelegramApiException {
        userMsgStart.setUserStep(chatId, step);
        deleteMessage(chatId, msgId);
        initMsgBooking(chatId, messages, link);
    }

    private void processingCallbackQueryRAA(long chatId,
                                            User user,
                                            List<Integer> messages,
                                            String data) throws TelegramApiException {
        if (data.startsWith(RAA + SET)) {
            processingRAA_SET(chatId, messages, data);
        } else
            switch (data) {
                case RAA_NEXT_APARTMENT -> {
                    userMsgChooseAnApartment.changeSelector(chatId, Selector.NEXT);
                    openMsgApartments(chatId, messages);
                }
                case RAA_PREVIOUS_APARTMENT -> {
                    userMsgChooseAnApartment.changeSelector(chatId, Selector.PREVIOUS);
                    openMsgApartments(chatId, messages);
                }
                case RAA_QUIT_FROM_CHOOSER_AN_APARTMENT -> {
                    userMsgChooseAnApartment.deleteTempApartmentSelector(chatId);
                    userMsgChooseCheckDate.deleteCheckOut(chatId);
                    userMsgStart.setUserStep(chatId, UserStep.EMPTY);
                    messages.add(userMsgChooseCheckDate.editMessage(chatId, USER_MSG_CHOOSE_CHECK_OUT_DATE));
                }
                case RAA_BOOK -> {
                    if (!userMsgChooseAnApartment.isBookingApartment(chatId)) {
                        userMsgChooseAnApartment.setIsBookingApartment(chatId, true);
                        userMsgStart.setUserStep(chatId, UserStep.SET_NAME);
                        initMsgBooking(chatId, messages, USER_MSG_SET_NAME);
                    } else
                        messages.add(userMsgCanNotBook.editMessage(chatId, USER_MSG_CAN_NOT_BOOK));
                }
                case RAA_QUIT_FROM_BOOKING_AN_APARTMENT -> {
                    userMsgChooseAnApartment.setIsBookingApartment(chatId, false);
                    userMsgChooseAnApartment.deleteTempApartmentSelector(chatId);
                    userMsgChooseAnApartment.addTempApartmentSelector(chatId);
                    openMsgApartments(chatId, messages);
                }
                case RAA_QUIT_CAN_NOT_BOOK -> {
                    userMsgChooseAnApartment.deleteTempApartmentSelector(chatId);
                    userMsgChooseAnApartment.addTempApartmentSelector(chatId);
                    openMsgApartments(chatId, messages);
                }
                case RAA_SHOW_PREVIEW -> {
                    userMsgStart.setUserStep(chatId, UserStep.EMPTY);
                    messages.add(userMsgPreviewCard.editMessage(
                            chatId, USER_MSG_SHOW_PREVIEW, userMsgPreviewCard.getPackParameters(chatId)));
                }
                case RAA_QUIT_FROM_PREVIEW_CARD -> {
                    userMsgStart.setUserStep(chatId, UserStep.COMPLETE);
                    initMsgBooking(chatId, messages, USER_MSG_BOOK);
                }
                case RAA_SEND_BOOKING_TO_ADMIN -> {
                    userMsgPreviewCard.insertCardToBookingCard(chatId, user);
                    messages.add(userMsgStart.editMessage(chatId, USER_MSG_START));
                    service.sendMessageInfo(service.getAdminId(), GENERAL_MSG_GOT_NEW_APP,
                            userMsgStart.getIKMarkupOkToDelete(service.getAdminId()));
                    
                    service.sendTempMessage(chatId, USER_MSG_SIMPLE_SEND_MESSAGE_TO_ADMIN, 10);
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
            case CHOOSE_CHECK_DATE -> openChooseCheckDate(chatId, user, messages, CheckDate.DATE);
            case CHANGE_CHECK_YEAR -> openChooseCheckDate(chatId, user, messages, CheckDate.YEAR);
            case CHANGE_CHECK_MONTH -> openChooseCheckDate(chatId, user, messages, CheckDate.MONTH);

            case NEXT_CHECK_YEAR_CM -> changeDate(chatId, messages, CheckDate.YEAR, Selector.NEXT, CheckDate.MONTH);
            case PREVIOUS_CHECK_YEAR_CM -> changeDate(chatId, messages, CheckDate.YEAR, Selector.PREVIOUS, CheckDate.MONTH);

            case QUIT_FROM_CHANGE_CHECK_MONTH -> openMsgChooseCheckDate(chatId, messages);

            case NEXT_CHECK_YEAR -> changeDate(chatId, messages, CheckDate.YEAR, Selector.NEXT, CheckDate.DATE);
            case PREVIOUS_CHECK_YEAR -> changeDate(chatId, messages, CheckDate.YEAR, Selector.PREVIOUS, CheckDate.DATE);

            case NEXT_CHECK_MONTH -> changeDate(chatId, messages, CheckDate.MONTH, Selector.NEXT, CheckDate.DATE);
            case PREVIOUS_CHECK_MONTH -> changeDate(chatId, messages, CheckDate.MONTH, Selector.PREVIOUS, CheckDate.DATE);

            case QUIT_FROM_CHOOSER_CHECK -> quitFromChooserCheck(chatId, messages);

            case ABOUT_US -> openAboutUs(chatId, messages);
            case CONTACTS -> openContacts(chatId, messages);
            case CHANGE_LANGUAGE -> openChangeLanguage(chatId, messages);
            case EN, TR, UK, RU -> changeLanguage(chatId, messages, data);
            case BACK_TO_START -> start(chatId, messages);
            case DELETE -> deleteMessage(chatId, msgId);
            default -> log.info("Unknown callback query data: {}", data);
        }
    }

    private void processingRAA_SET(long chatId, List<Integer> messages, String data) throws TelegramApiException {
        if (data.startsWith(RAA_SET_YEAR)) {
            userMsgChangeCheckYear.setYear(chatId, Integer.parseInt(data.replaceAll(RAA_SET_YEAR, "")));
            openMsgChooseCheckDate(chatId, messages);
        } else if (data.startsWith(RAA_SET_DAY)) {
            if (!userMsgChooseCheckDate.isCheckInSet(chatId)) {
                userMsgChooseCheckDate.setCheck(chatId,
                        Integer.parseInt(data.replaceAll(RAA_SET_DAY, "")), CheckDate.IN);
                messages.add(userMsgChooseCheckDate.editMessage(chatId, USER_MSG_CHOOSE_CHECK_OUT_DATE));
            } else {
                userMsgChooseCheckDate.setCheck(chatId,
                        Integer.parseInt(data.replaceAll(RAA_SET_DAY, "")), CheckDate.OUT);
                userMsgChooseAnApartment.addTempApartmentSelector(chatId);
                openMsgApartments(chatId, messages);
            }
        } else {
            switch (data) {
                case RAA_SET_JANUARY -> userMsgChooseCheckDate.setSelectedMonth(chatId, Month.JANUARY.getMonth());
                case RAA_SET_FEBRUARY -> userMsgChooseCheckDate.setSelectedMonth(chatId, Month.FEBRUARY.getMonth());
                case RAA_SET_MARCH -> userMsgChooseCheckDate.setSelectedMonth(chatId, Month.MARCH.getMonth());
                case RAA_SET_APRIL -> userMsgChooseCheckDate.setSelectedMonth(chatId, Month.APRIL.getMonth());
                case RAA_SET_MAY -> userMsgChooseCheckDate.setSelectedMonth(chatId, Month.MAY.getMonth());
                case RAA_SET_JUNE -> userMsgChooseCheckDate.setSelectedMonth(chatId, Month.JUNE.getMonth());
                case RAA_SET_JULY -> userMsgChooseCheckDate.setSelectedMonth(chatId, Month.JULY.getMonth());
                case RAA_SET_AUGUST -> userMsgChooseCheckDate.setSelectedMonth(chatId, Month.AUGUST.getMonth());
                case RAA_SET_SEPTEMBER -> userMsgChooseCheckDate.setSelectedMonth(chatId, Month.SEPTEMBER.getMonth());
                case RAA_SET_OCTOBER -> userMsgChooseCheckDate.setSelectedMonth(chatId, Month.OCTOBER.getMonth());
                case RAA_SET_NOVEMBER -> userMsgChooseCheckDate.setSelectedMonth(chatId, Month.NOVEMBER.getMonth());
                case RAA_SET_DECEMBER -> userMsgChooseCheckDate.setSelectedMonth(chatId, Month.DECEMBER.getMonth());
                default -> log.warn("Unknown RAA_SET data: {}", data);
            }
            openMsgChooseCheckDate(chatId, messages);
        }
    }

    private void openAboutUs(long chatId, List<Integer> messages) throws TelegramApiException {
        messages.add(userMsgAboutUs.editMessage(chatId, USER_MSG_ABOUT_US));
    }

    private void openContacts(long chatId, List<Integer> messages) throws TelegramApiException {
        messages.add(userMsgContacts.editMessage(chatId, USER_MSG_CONTACTS, service.getAdminContacts()));
    }

    private void openChangeLanguage(long chatId, List<Integer> messages) throws TelegramApiException {
        messages.add(userMsgChangeLanguage.editMessage(chatId, GENERAL_MSG_CHANGE_LANGUAGE));
    }

    private void changeLanguage(long chatId, List<Integer> messages, String data) throws TelegramApiException {
        service.setLanguage(chatId, data);
        start(chatId, messages);
    }

    private void cardDataProcessing(long chatId,
                                    int msgId,
                                    List<Integer> messages,
                                    String data,
                                    Card type) throws TelegramApiException {
        if (chatId == userMsgStart.getBookingUserIdApartment(chatId))
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
                    if (data.matches("[MWmw]"))
                        correctInputCard(chatId, msgId, messages, data.toUpperCase(), type);
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
        else
            deleteMessage(chatId, msgId);
    }

    private void correctInputCard(long chatId,
                                  int msgId,
                                  List<Integer> messages,
                                  String data,
                                  Card type) throws TelegramApiException {

        deleteMessage(chatId, msgId);

        switch (type) {
            case NAME -> {
                userMsgBooking.setName(chatId, data);
                userMsgStart.setUserStep(chatId, UserStep.SET_SURNAME);
                initMsgBooking(chatId, messages, USER_MSG_SET_SURNAME);
            }
            case SURNAME -> {
                userMsgBooking.setSurname(chatId, data);
                userMsgStart.setUserStep(chatId, UserStep.SET_GENDER);
                initMsgBooking(chatId, messages, USER_MSG_SET_GENDER);
            }
            case GENDER -> {
                userMsgBooking.setGender(chatId, data);
                userMsgStart.setUserStep(chatId, UserStep.SET_AGE);
                initMsgBooking(chatId, messages, USER_MSG_SET_AGE);
            }
            case AGE -> {
                userMsgBooking.setAge(chatId, data);
                userMsgStart.setUserStep(chatId, UserStep.SET_COUNT);
                initMsgBooking(chatId, messages, USER_MSG_SET_COUNT);
            }
            case COUNT -> {
                userMsgBooking.setCount(chatId, data);
                userMsgStart.setUserStep(chatId, UserStep.SET_CONTACTS);
                initMsgBooking(chatId, messages, USER_MSG_SET_CONTACTS);
            }
            case CONTACTS -> {
                userMsgBooking.setContacts(chatId, data);
                userMsgStart.setUserStep(chatId, UserStep.COMPLETE);
                initMsgBooking(chatId, messages, USER_MSG_BOOK);
            }
        }
    }

    private void start(long chatId, User user, List<Integer> messages) throws TelegramApiException {
        service.authorization(chatId, user);

        messages.add(userMsgStart.editMessage(chatId, USER_MSG_START));
    }

    private void start(long chatId, List<Integer> messages) throws TelegramApiException {
        messages.add(userMsgStart.editMessage(chatId, USER_MSG_START));
    }

    private void deleteMessage(long chatId, int msgId) {
        service.deleteMessage(chatId, msgId);
    }

    private void openChooseCheckDate(long chatId,
                                     User user,
                                     List<Integer> messages,
                                     CheckDate type) throws TelegramApiException {
        switch (type) {
            case DATE -> initSelectorCheckDate(chatId, user, messages);
            case YEAR -> messages.add(userMsgChangeCheckYear.editMessage(chatId, USER_MSG_CHANGE_CHECK_YEAR));
            case MONTH -> messages.add(userMsgChangeCheckMonth.editMessage(chatId, USER_MSG_CHANGE_CHECK_MONTH));
        }
    }

    private void initSelectorCheckDate(long chatId, User user, List<Integer> messages) throws TelegramApiException {
        if (userMsgChooseCheckDate.isAlreadyExistRent(chatId))
            messages.add(userMsgAlreadyRenting.editMessage(chatId, USER_MSG_ALREADY_EXIST_RENT));
        else {
            userMsgChooseCheckDate.addNewUserToTempBookingData(chatId, user);
            messages.add(userMsgChooseCheckDate.editMessage(chatId, USER_MSG_CHOOSE_CHECK_IN_DATE));
        }
    }

    private void changeDate(long chatId,
                            List<Integer> messages,
                            CheckDate date,
                            Selector selector,
                            CheckDate origin) throws TelegramApiException {
        switch (origin) {
            case MONTH -> {
                userMsgChooseCheckDate.changeDate(chatId, date, selector);
                messages.add(userMsgChangeCheckMonth.editMessage(chatId, USER_MSG_CHANGE_CHECK_MONTH));
            }
            case DATE -> {
                userMsgChooseCheckDate.changeDate(chatId, date, selector);
                openMsgChooseCheckDate(chatId, messages);
            }
        }
    }

    private void quitFromChooserCheck(long chatId, List<Integer> messages) throws TelegramApiException {
        if (userMsgChooseCheckDate.isCheckInSet(chatId)) {
            userMsgChooseCheckDate.deleteCheckIn(chatId);
            messages.add(userMsgChooseCheckDate.editMessage(chatId, USER_MSG_CHOOSE_CHECK_IN_DATE));
        } else {
            userMsgChooseCheckDate.deleteUserFromTempBookingData(chatId);
            start(chatId, messages);
        }
    }

    private void openMsgChooseCheckDate(long chatId, List<Integer> messages) throws TelegramApiException {
        String message = userMsgChooseCheckDate.isCheckInSet(chatId) ?
                USER_MSG_CHOOSE_CHECK_OUT_DATE : USER_MSG_CHOOSE_CHECK_IN_DATE;

        messages.add(userMsgChooseCheckDate.editMessage(chatId, message));
    }



    private void openMsgApartments(long chatId, List<Integer> messages) throws TelegramApiException {
        int msgId = service.sendSimpleMessage(chatId, GENERAL_MSG_SIMPLE_DOWNLOADING);

        if (!userMsgStart.isApartmentsEmpty(chatId)) {
            messages.addAll(userMsgChooseAnApartment.sendPhotos(chatId,
                    service.getApartmentPath() + userMsgChooseAnApartment.getCurrentApartment(chatId)));

            messages.add(userMsgChooseAnApartment.sendMessage(chatId, USER_MSG_CHOOSE_AN_APARTMENT,
                    userMsgStart.getApartmentParameters(chatId)));
        } else
            messages.add(userMsgNoFreeApartment.sendMessage(chatId, USER_MSG_NO_FREE_APARTMENTS));

        deleteMessage(chatId, msgId);
    }

    private void initMsgBooking(long chatId, List<Integer> messages, String msgLink) throws TelegramApiException {
        messages.add(userMsgBooking.editMessage(chatId, msgLink, userMsgBooking.getTempBookingData(chatId)));
    }

    private void initIncorrectEnterCard(long chatId, int msgId, String msgLink) {
        try {
            service.lateDeleteMessage(chatId, msgId, 10);
            service.lateDeleteMessage(chatId, service.sendSimpleMessage(chatId, msgLink, 10), 10);
        } catch (TelegramApiException ex) {
            log.warn("""
                    sendSimpleMessage(Update, String) can't send msgLink {} for user {}
                    Exception: {}""", msgLink, chatId, ex.getMessage());
        }
    }
}
