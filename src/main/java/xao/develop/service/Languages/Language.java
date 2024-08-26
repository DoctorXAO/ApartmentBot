package xao.develop.service.Languages;

public interface Language {
    String getStart();
    String getHouseInformation();
    String getContacts(String phone, String email);
    String getRules();
    String getApartments();
    String getRentAnApartment();
    String getFillOutAnApplication1();
    String getFillOutAnApplication2();
    String getFillOutAnApplication3(String[] prices);
    String getFillOutAnApplication4();
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
