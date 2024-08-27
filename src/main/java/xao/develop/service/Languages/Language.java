package xao.develop.service.Languages;

public interface Language {
    String getStart();
    String getHouseInformation();
    String getContacts(String phone, String email);
    String getRules();
    String getApartments();
    String getRentAnApartment();
    String getFillOutName();
    String getFillOutCountOfPerson();
    String getFillOutRentTime(String onePerDay,
                              String onePerMouth,
                              String onePerYear,
                              String twoPerDay,
                              String twoPerMouth,
                              String twoPerYear);
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
