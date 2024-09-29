package xao.develop.config;

public interface UserCommand {
    String START = "/start";
    String CARD_NAME = "/name";
    String CARD_SURNAME = "/surname";
    String CARD_GENDER = "/gender";
    String CARD_AGE = "/age";
    String CARD_COUNT = "/count";
    String CARD_CONTACTS = "/contacts";

    String RAA = "raa_";
    String RAA_CHOOSE_CHECK_DATE = RAA + "choose_check_date";
    String RAA_CHANGE_CHECK_MONTH = RAA + "change_check_month";
    String RAA_NEXT_CHECK_YEAR_CM = RAA + "next_check_year_cm";
    String RAA_PREVIOUS_CHECK_YEAR_CM = RAA + "previous_check_year_cm";
    String RAA_QUIT_FROM_CHANGE_CHECK_MONTH = RAA + "quit_from_change_check_month";
    String RAA_CHANGE_CHECK_YEAR = RAA + "change_check_year";
    String RAA_NEXT_CHECK_YEAR = RAA + "next_check_year";
    String RAA_PREVIOUS_CHECK_YEAR = RAA + "previous_check_year";
    String RAA_NEXT_CHECK_MONTH = RAA + "next_check_month";
    String RAA_PREVIOUS_CHECK_MONTH = RAA + "previous_check_month";
    String RAA_QUIT_FROM_CHOOSER_CHECK = RAA + "quit_from_chooser_check";

    String RAA_CHOOSE_CHECK_OUT_DATE = RAA + "choose_check_out_date";

    String RAA_CHOOSE_AN_APARTMENT = RAA + "choose_an_apartment";
    String RAA_NEXT_APARTMENT = RAA + "next_apartment";
    String RAA_PREVIOUS_APARTMENT = RAA + "previous_apartment";
    String RAA_QUIT_FROM_CHOOSER_AN_APARTMENT = RAA + "quit_from_chooser_an_apartment";

    String RAA_BOOK = RAA + "book";
    String RAA_SHOW_PREVIEW = RAA + "show_preview";

    String RAA_QUIT_FROM_BOOKING_AN_APARTMENT = RAA + "quit_from_booking_an_apartment";
    String RAA_QUIT_CAN_NOT_BOOK = RAA + "quit_can_not_book";
    String RAA_QUIT_FROM_PREVIEW_CARD = RAA + "quit_from_preview_card";

    String RAA_SEND_BOOKING_TO_ADMIN = RAA + "send_booking_to_admin";

    String ABOUT_US = "house_information";

    String CONTACTS = "contacts";

    String CHANGE_LANGUAGE = "change_language";
    String TR = "tr";
    String EN = "en";
    String RU = "ru";

    String BACK = "back";
    String BACK_TO_START = "back_to_start";
    String NEXT = "next";
    String SEND = "send";

    String EMPTY = "empty";

    String SET = "set_";
    String RAA_SET_MONTH = RAA + SET + "month_";
    String RAA_SET_JANUARY = RAA_SET_MONTH + "1";
    String RAA_SET_FEBRUARY = RAA_SET_MONTH + "2";
    String RAA_SET_MARCH = RAA_SET_MONTH + "3";
    String RAA_SET_APRIL = RAA_SET_MONTH + "4";
    String RAA_SET_MAY = RAA_SET_MONTH + "5";
    String RAA_SET_JUNE = RAA_SET_MONTH + "6";
    String RAA_SET_JULY = RAA_SET_MONTH + "7";
    String RAA_SET_AUGUST = RAA_SET_MONTH + "8";
    String RAA_SET_SEPTEMBER = RAA_SET_MONTH + "9";
    String RAA_SET_OCTOBER = RAA_SET_MONTH + "10";
    String RAA_SET_NOVEMBER = RAA_SET_MONTH + "11";
    String RAA_SET_DECEMBER = RAA_SET_MONTH + "12";
    String RAA_SET_YEAR = RAA + SET + "year_";
    String RAA_SET_DAY = RAA + SET + "day_";

    String NO_FREE_APARTMENTS = "no_free_apartments";
    String CAN_NOT_BOOK = "can_not_book";
}
