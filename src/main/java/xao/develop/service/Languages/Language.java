package xao.develop.service.Languages;

import xao.develop.config.BotConfig;

public interface Language {
    String getStart();
    String getHouseInformation();
    String getContacts(String phone, String email);
    String getRules();
    String getApartments();
    String getRentAnApartment();
    String getFillOutName();
    String getFillOutCountOfPerson();
    String getFillOutRentTime();
    String getFillOutCommentary();
    String getChangeLanguage();

    String getButtonBack();
    String getButtonApartments();
    String getButtonRentAnApartment();
    String getButtonHouseInformation();
    String getButtonContacts();
    String getButtonChangeLanguage();
    String getButtonRules();
    String getButtonFillOutAnApplication();
}
