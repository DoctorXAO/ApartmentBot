package xao.develop.service;

public interface Language {
    String getStart();
    String getHouseInformation();
    String getContacts(String phone, String email);
    String getRules();
    String getApartments();
    String getRentAnApartment();
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
