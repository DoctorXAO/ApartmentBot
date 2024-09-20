package xao.develop.config;

public interface UserCommand {
    String START = "/start";

    String APARTMENTS = "apartments";

    String RAA = "raa_";
    String RAA_RENT_AN_APARTMENT = RAA + "rent_an_apartment";

    String RAA_CHOOSE_CHECK_IN_DATE = RAA + "choose_check_in_date";
    String RAA_CHANGE_CHECK_IN_MONTH = RAA + "change_check_in_month";
    String RAA_CHANGE_CHECK_IN_YEAR = RAA + "change_check_in_year";
    String RAA_NEXT_CHECK_IN_YEAR = RAA + "next_check_in_year";
    String RAA_PREVIOUS_CHECK_IN_YEAR = RAA + "previous_check_in_year";
    String RAA_NEXT_CHECK_IN_MONTH = RAA + "next_check_in_month";
    String RAA_PREVIOUS_CHECK_IN_MONTH = RAA + "previous_check_in_month";
    String RAA_QUIT_FROM_CHOOSER_CHECK_IN = RAA + "quit_from_chooser_check_in";

    String RAA_CHOOSE_CHECK_OUT_DATE = RAA + "choose_check_in_date";

    String RAA_CHOOSE_AN_APARTMENT = RAA + "choose_an_apartment";
    String RAA_NEXT_APARTMENT = RAA + "next_apartment";
    String RAA_PREVIOUS_APARTMENT = RAA + "previous_apartment";
    String RAA_QUIT_FROM_CHOOSER_AN_APARTMENT = RAA + "quit_from_chooser_an_apartment";

    String RAA_BOOK = RAA + "book";

    String HI = "hi_";
    String HI_HOUSE_INFORMATION = HI + "house_information";
    String HI_RULES = HI + "rules";

    String CONTACTS = "contacts";

    String CHANGE_LANGUAGE = "change_language";
    String TR = "tr";
    String EN = "en";
    String RU = "ru";

    String BACK = "back";
    String BACK_TO_START = "back_to_start";

    String EMPTY = "empty";
    String DAYS = "day_";
}
