package xao.develop.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import xao.develop.model.*;
import xao.develop.enums.AppStatus;

import java.util.Calendar;
import java.util.List;
import java.util.Set;

@Slf4j
@Repository
public class Persistence {

    @Autowired
    private ServerStatusRepository serverStatusRepository;

    @Autowired
    private AccountStatusRepository accountStatusRepository;

    @Autowired
    private ApartmentRepository apartmentRepository;

    @Autowired
    private BookingCardRepository bookingCardRepository;

    @Autowired
    private TempBotMessageRepository tempBotMessageRepository;

    @Autowired
    private TempBookingDataRepository tempBookingDataRepository;

    @Autowired
    private TempApartmentSelectorRepository tempApartmentSelectorRepository;

    @Autowired
    private AmenityRepository amenityRepository;

    @Autowired
    private TempAdminSettingsRepository tempAdminSettingsRepository;

    @Autowired
    private TempNewApartmentRepository tempNewApartmentRepository;

    @Autowired
    private TempSelectedAmenityRepository tempSelectedAmenityRepository;

    // AccountStatuses

    public void insertAccountStatus(Long chatId,
                                    String language) {
        AccountStatus accountStatus = new AccountStatus();

        accountStatus.setChatId(chatId);
        accountStatus.setLanguage(language);

        accountStatusRepository.save(accountStatus);
    }

    public AccountStatus selectAccountStatus(Long chatId) {
        return accountStatusRepository.getByChatId(chatId);
    }

    /** Обновляет язык интерфейса пользователя **/
    public void updateLanguageInAccountStatus(Long chatId, String language) {
        AccountStatus accountStatus = accountStatusRepository.findById(chatId).orElseThrow();

        accountStatus.setLanguage(language);

        accountStatusRepository.save(accountStatus);
    }

    // TempBotMessages

    public List<TempBotMessage> selectTempBotMessages(Long chatID) {
        return tempBotMessageRepository.findByChatId(chatID);
    }

    public List<Long> selectDistinctChatIdsTempBotMessages() {
        return tempBotMessageRepository.findDistinctChatIds();
    }

    public void insertTempBotMessage(long chatId, int messageId) {
        TempBotMessage tempBotMessage = new TempBotMessage();
        tempBotMessage.setChatId(chatId);
        tempBotMessage.setMsgId(messageId);

        tempBotMessageRepository.save(tempBotMessage);
    }

    public void deleteTempBotMessages(long chatId) {
        tempBotMessageRepository.deleteByChatId(chatId);
    }

    public void deleteMessageTempBotMessage(long chatId, int msgId) {
        tempBotMessageRepository.deleteByChatIdAndMsgId(chatId, msgId);
    }

    // Apartment

    public void insertApartment(int number, double area, List<Amenity> amenities) {
        Apartment apartment = new Apartment();

        apartment.setNumber(number);
        apartment.setArea(area);
        apartment.setAmenities(amenities);

        apartmentRepository.save(apartment);
    }

    public Apartment selectApartment(int number) {
        return apartmentRepository.getByNumber(number);
    }

    public List<Apartment> selectAllApartmentsSortByNumber() {
        return apartmentRepository.findAll(Sort.by(Sort.Direction.ASC, "number"));
    }

    public List<Apartment> selectAllFreeApartments(long chatId) {
        List<Apartment> apartments = selectAllApartmentsSortByNumber();

        apartments.removeIf(Apartment::getIsBooking);

        List<BookingCard> bookingCards = selectBookingCardByStatus(AppStatus.WAITING);
        bookingCards.addAll(selectBookingCardByStatus(AppStatus.ACCEPTED));
        TempBookingData tempBookingData = selectTempBookingData(chatId);

        for (BookingCard bookingCard : bookingCards) {
            long bc_ci = bookingCard.getCheckIn();
            long bc_co = bookingCard.getCheckOut();
            long tb_ci = tempBookingData.getCheckIn();
            long tb_co = tempBookingData.getCheckOut();
            int numberOfApartment = bookingCard.getNumberOfApartment();

            log.debug("""
                    Extra:
                    nOa: {}
                    bc_ci: {}
                    bc_co: {}
                    tb_ci: {}
                    tb_co: {}
                    """, numberOfApartment, bc_ci, bc_co, tb_ci, tb_co);

            if (bc_co >= tb_ci && bc_ci <= tb_co)
                apartments.removeIf(apartment -> apartment.getNumber() == numberOfApartment);
        }

        for (int i = 0; i < apartments.size(); i++)
            log.debug("Apartment №{}: {} number of room", i, apartments.get(i).getNumber());

        return apartments;
    }

    public void updateIsBookingApartment(int number, boolean isBooking, long userId) {
        Apartment apartment = selectApartment(number);

        apartment.setIsBooking(isBooking);
        
        if (isBooking)
            apartment.setUserId(userId);
        else
            apartment.setUserId(null);

        apartmentRepository.save(apartment);
    }

    // BookingCard

    public void insertBookingCard(long chatId,
                                  String login,
                                  String firstName,
                                  String lastName,
                                  String contacts,
                                  int age,
                                  String gender,
                                  int countOfPeople,
                                  int numberOfApartment,
                                  Long checkIn,
                                  Long checkOut,
                                  int cost) {
        BookingCard bookingCard = new BookingCard();

        bookingCard.setChatId(chatId);
        bookingCard.setLogin(login);
        bookingCard.setFirstName(firstName);
        bookingCard.setLastName(lastName);
        bookingCard.setContacts(contacts);
        bookingCard.setAge(age);
        bookingCard.setGender(gender);
        bookingCard.setCountOfPeople(countOfPeople);
        bookingCard.setNumberOfApartment(numberOfApartment);
        bookingCard.setCheckIn(checkIn);
        bookingCard.setCheckOut(checkOut);
        bookingCard.setStatus(AppStatus.WAITING.getType());
        bookingCard.setCost(cost);

        bookingCardRepository.save(bookingCard);
    }

    public BookingCard selectBookingCard(int idOfCard) {
        log.debug("Select booking card: {}", idOfCard);

        return bookingCardRepository.findById(idOfCard);
    }

    public List<BookingCard> selectBookingCardByStatus(AppStatus type) {
        return bookingCardRepository.findAllByStatus(type.getType());
    }

    public List<BookingCard> selectAllBookingCardExceptWaiting() {
        return bookingCardRepository.findAllExceptWaiting();
    }

    public void updateBookingCard(int idOfCard, AppStatus status) {
        BookingCard bookingCard = bookingCardRepository.findById(idOfCard);

        bookingCard.setStatus(status.getType());

        bookingCardRepository.save(bookingCard);
    }

    // TempBookingData

    public void insertTempBookingData(long chatId, long selectedTime, String login) {
        TempBookingData tempBookingData = new TempBookingData();

        tempBookingData.setChatId(chatId);
        tempBookingData.setSelectedTime(selectedTime);
        tempBookingData.setLogin(login);

        tempBookingDataRepository.save(tempBookingData);

        log.debug("Added new user to TempBookingData: {}", chatId);
    }

    public TempBookingData selectTempBookingData(long chatId) {
        log.debug("Select user from TempBookingDataRepository: {}", chatId);

        return tempBookingDataRepository.getByChatId(chatId);
    }

    public void updateSelectedTimeInTempBookingData(long chatId, long selectedTime) {
        log.trace("Method updateSelectedTimeInTempBookingData(long, long) started");

        TempBookingData tempBookingData = tempBookingDataRepository.findById(chatId).orElseThrow();

        log.debug("TempBookingData is updating for user: {}. Old value of selectedTime: {}", chatId, tempBookingData.getSelectedTime());

        tempBookingData.setSelectedTime(selectedTime);

        tempBookingDataRepository.save(tempBookingData);

        log.debug("TempBookingData updated for user: {}. New value of selectedTime: {}", chatId, tempBookingData.getSelectedTime());
        log.trace("Method updateSelectedTimeInTempBookingData(long, long) finished");
    }

    public void updateCheckInInTempBookingData(long chatId, long checkIn) {
        log.trace("Method updateCheckInInTempBookingData(long, long) started");

        TempBookingData tempBookingData = tempBookingDataRepository.findById(chatId).orElseThrow();

        tempBookingData.setCheckIn(checkIn);

        tempBookingDataRepository.save(tempBookingData);

        log.trace("Method updateCheckInInTempBookingData(long, long) finished");
    }

    public void updateCheckOutInTempBookingData(long chatId, long checkOut) {
        log.trace("Method updateCheckOutInTempBookingData(long, long) started");

        TempBookingData tempBookingData = tempBookingDataRepository.findById(chatId).orElseThrow();

        tempBookingData.setCheckOut(checkOut);

        tempBookingDataRepository.save(tempBookingData);

        log.trace("Method updateCheckOutInTempBookingData(long, long) finished");
    }

    public void updateNumberOfApartmentTempBookingData(long chatId, int numberOfApartment) {
        log.trace("Method updateNumberOfApartmentTempBookingData(long, int) started");

        TempBookingData tempBookingData = tempBookingDataRepository.findById(chatId).orElseThrow();

        tempBookingData.setNumberOfApartment(numberOfApartment);

        tempBookingDataRepository.save(tempBookingData);

        log.trace("Method updateNumberOfApartmentTempBookingData(long, int) finished");
    }

    public void updateFirstNameTempBookingData(long chatId, String firstName) {
        log.trace("Method updateFirstNameTempBookingData(long, String) started");

        TempBookingData tempBookingData = tempBookingDataRepository.findById(chatId).orElseThrow();

        tempBookingData.setFirstName(firstName);

        tempBookingDataRepository.save(tempBookingData);

        log.trace("Method updateFirstNameTempBookingData(long, String) finished");
    }

    public void updateLastNameTempBookingData(long chatId, String lastName) {
        log.trace("Method updateLastNameTempBookingData(long, String) started");

        TempBookingData tempBookingData = tempBookingDataRepository.findById(chatId).orElseThrow();

        tempBookingData.setLastName(lastName);

        tempBookingDataRepository.save(tempBookingData);

        log.trace("Method updateLastNameTempBookingData(long, String) finished");
    }

    public void updateContactsTempBookingData(long chatId, String contacts) {
        log.trace("Method updateContactsTempBookingData(long, String) started");

        TempBookingData tempBookingData = tempBookingDataRepository.findById(chatId).orElseThrow();

        tempBookingData.setContacts(contacts);

        tempBookingDataRepository.save(tempBookingData);

        log.trace("Method updateContactsTempBookingData(long, String) finished");
    }

    public void updateAgeTempBookingData(long chatId, String age) {
        log.trace("Method updateAgeTempBookingData(long, String) started");

        TempBookingData tempBookingData = tempBookingDataRepository.findById(chatId).orElseThrow();

        tempBookingData.setAge(Integer.parseInt(age));

        tempBookingDataRepository.save(tempBookingData);

        log.trace("Method updateAgeTempBookingData(long, String) finished");
    }

    public void updateGenderTempBookingData(long chatId, String gender) {
        log.trace("Method updateGenderTempBookingData(long, String) started");

        TempBookingData tempBookingData = tempBookingDataRepository.findById(chatId).orElseThrow();

        tempBookingData.setGender(gender);

        tempBookingDataRepository.save(tempBookingData);

        log.trace("Method updateGenderTempBookingData(long, String) finished");
    }

    public void updateCountOfPeopleTempBookingData(long chatId, String countOfPeople) {
        log.trace("Method updateCountOfPeopleTempBookingData(long, String) started");

        TempBookingData tempBookingData = tempBookingDataRepository.findById(chatId).orElseThrow();

        tempBookingData.setCountOfPeople(Integer.parseInt(countOfPeople));

        tempBookingDataRepository.save(tempBookingData);

        log.trace("Method updateCountOfPeopleTempBookingData(long, String) finished");
    }

    public void deleteTempBookingData(long chatId) {
        log.trace("Method deleteTempBookingData(long) started");

        tempBookingDataRepository.deleteById(chatId);

        log.debug("The next user deleted from TempBookingData: {}", chatId);
        log.trace("Method deleteTempBookingData(long) finished");
    }

    public void deleteCheckInInTempBookingData(long chatId) {
        log.trace("Method deleteCheckInInTempBookingData(long) started");

        TempBookingData tempBookingData = tempBookingDataRepository.findById(chatId).orElseThrow();

        tempBookingData.setCheckIn(null);

        tempBookingDataRepository.save(tempBookingData);

        log.trace("Method deleteCheckInInTempBookingData(long) finished");
    }

    public void deleteCheckOutInTempBookingData(long chatId) {
        log.trace("Method deleteCheckOutInTempBookingData(long) started");

        TempBookingData tempBookingData = tempBookingDataRepository.findById(chatId).orElseThrow();

        tempBookingData.setCheckOut(null);

        tempBookingDataRepository.save(tempBookingData);

        log.trace("Method deleteCheckOutInTempBookingData(long) finished");
    }

    // Amenity

    public List<Amenity> selectAllAmenities() {
        return amenityRepository.findAll();
    }

    public Amenity selectAmenity(int idAmenity) {
        return amenityRepository.getByIdAmenity(idAmenity);
    }

    // TempApartmentSelector

    public void insertTempApartmentSelector(long chatId) {
        List<Apartment> apartments = selectAllFreeApartments(chatId);

        if (!apartments.isEmpty()) {
            TempApartmentSelector tempApartmentSelector = new TempApartmentSelector();

            tempApartmentSelector.setChatId(chatId);
            tempApartmentSelector.setNumberOfApartment(apartments.get(0).getNumber());
            tempApartmentSelector.setSelector(0);

            tempApartmentSelectorRepository.save(tempApartmentSelector);
        } else {
            log.warn("Array of apartments is empty");
        }
    }

    public TempApartmentSelector selectTempApartmentSelector(long chatId) {
        return tempApartmentSelectorRepository.findByChatId(chatId);
    }

    public void updateTempApartmentSelector(long chatId, int numberOfApartment, int selector) {
        TempApartmentSelector tempApartmentSelector = tempApartmentSelectorRepository.findByChatId(chatId);

        tempApartmentSelector.setNumberOfApartment(numberOfApartment);
        tempApartmentSelector.setSelector(selector);

        tempApartmentSelectorRepository.save(tempApartmentSelector);
    }

    public void deleteTempApartmentSelector(long chatId) {
        tempApartmentSelectorRepository.deleteByChatId(chatId);
    }

    // TempAdminSettings

    public void insertTempAdminSettings(long chatId) {
        TempAdminSettings tempAdminSettings = new TempAdminSettings();

        tempAdminSettings.setChatId(chatId);

        tempAdminSettingsRepository.save(tempAdminSettings);
    }

    public TempAdminSettings selectTempAdminSettings(long chatId) {
        return tempAdminSettingsRepository.findById(chatId);
    }

    public void updateSelectedAppTempAdminSettings(long chatId, int selectedApp) {
        TempAdminSettings tempAdminSettings;

        try {
            tempAdminSettings = tempAdminSettingsRepository.findById(chatId);

            tempAdminSettings.setSelectedApplication(selectedApp);
        }
        catch (NullPointerException ex) {
            tempAdminSettings = new TempAdminSettings();

            tempAdminSettings.setChatId(chatId);
            tempAdminSettings.setSelectedApplication(selectedApp);
        }

        tempAdminSettingsRepository.save(tempAdminSettings);
    }

    public void updateSelectedPageTempAdminSettings(long chatId, int selectedPage) {
        TempAdminSettings tempAdminSettings = tempAdminSettingsRepository.findById(chatId);

        tempAdminSettings.setSelectedPage(selectedPage);

        tempAdminSettingsRepository.save(tempAdminSettings);
    }

    public void updateNewApartmentTempAdminSettings(long chatId, boolean isNewApartment) {
        TempAdminSettings tempAdminSettings = tempAdminSettingsRepository.findById(chatId);

        tempAdminSettings.setNewApartment(isNewApartment);

        tempAdminSettingsRepository.save(tempAdminSettings);
    }

    public void updateCheckingSelectedAmenitiesTempAdminSettings(long chatId, boolean isCheckingSelectedAmenities) {
        TempAdminSettings tempAdminSettings = tempAdminSettingsRepository.findById(chatId);

        tempAdminSettings.setCheckingSelectedAmenities(isCheckingSelectedAmenities);

        tempAdminSettingsRepository.save(tempAdminSettings);
    }

    public void deleteTempAdminSettings(long chatId) {
        tempAdminSettingsRepository.deleteById(chatId);
    }

    public void resetToDefaultTempAdminSettings() {
        tempAdminSettingsRepository.resetToDefault();
    }

    // TempNewApartment

    public void insertTempNewApartment(long chatId) {
        TempNewApartment tempNewApartment = new TempNewApartment();

        tempNewApartment.setChatId(chatId);

        tempNewApartmentRepository.save(tempNewApartment);
    }

    public TempNewApartment selectTempNewApartment(long chatId) {
        return tempNewApartmentRepository.findByChatId(chatId);
    }

    public void updateNumberTempNewApartment(long chatId, int number) {
        TempNewApartment tempNewApartment = selectTempNewApartment(chatId);

        tempNewApartment.setNumber(number);

        tempNewApartmentRepository.save(tempNewApartment);
    }

    public void updateCountOfPicturesTempNewApartment(long chatId, long countOfPictures) {
        TempNewApartment tempNewApartment = selectTempNewApartment(chatId);

        tempNewApartment.setCountOfPictures(countOfPictures);

        tempNewApartmentRepository.save(tempNewApartment);
    }

    public void updateAreaTempNewApartment(long chatId, double area) {
        TempNewApartment tempNewApartment = selectTempNewApartment(chatId);

        tempNewApartment.setArea(area);

        tempNewApartmentRepository.save(tempNewApartment);
    }

    public void deleteTempNewApartment(long chatId) {
        tempNewApartmentRepository.deleteById(chatId);
    }

    // TempSelectedAmenity

    public void insertTempSelectedAmenity(long chatId, int idOfAmenity) {
        TempSelectedAmenity tempSelectedAmenity = new TempSelectedAmenity();

        tempSelectedAmenity.setChatId(chatId);
        tempSelectedAmenity.setIdOfAmenity(idOfAmenity);

        tempSelectedAmenityRepository.save(tempSelectedAmenity);
    }

    public List<TempSelectedAmenity> selectAllSelectedAmenities(long chatId) {
        return tempSelectedAmenityRepository.findAllByChatId(chatId);
    }

    public List<TempSelectedAmenity> selectAllSelectedAmenitiesExceptTheId(long chatId, int idOfAmenity) {
        return tempSelectedAmenityRepository.findAllByChatIdAndIdOfAmenityNot(chatId, idOfAmenity);
    }

    public void deleteTempSelectedAmenity(long chatId, int idOfAmenity) {
        tempSelectedAmenityRepository.deleteByIdOfAmenity(idOfAmenity);
    }

    public void deleteAllTempSelectedAmenity(long chatId) {
        tempSelectedAmenityRepository.deleteAllByChatId(chatId);

        log.debug("Selected amenities deleted!");
    }

    // other

    public void setPresentTime() {
        Calendar presentDate = Calendar.getInstance();

        presentDate.set(Calendar.HOUR_OF_DAY, 0);
        presentDate.set(Calendar.MINUTE, 0);
        presentDate.set(Calendar.SECOND, 0);
        presentDate.set(Calendar.MILLISECOND, 0);

        ServerStatus serverStatus = new ServerStatus();
        serverStatus.setCode("MAIN");
        serverStatus.setPresentTime(presentDate.getTimeInMillis());
        serverStatusRepository.save(serverStatus);

        log.info("The bot's present time was set with the following settings: {}", presentDate);
        log.info("The bot's present time in milliseconds was set: {}", presentDate.getTimeInMillis());
    }

    public void clearTempDAO() {
        tempBookingDataRepository.deleteAll();
        tempApartmentSelectorRepository.deleteAll();
        apartmentRepository.resetToDefault();

        log.info("All tempDAO cleared");
    }

    public Calendar getServerPresentTime() {
        ServerStatus serverStatus = serverStatusRepository.getByCode("MAIN");

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(serverStatus.getPresentTime());

        return calendar;
    }

    public void freeUpTheVacatedApartments() {
        List<BookingCard> bookingCards = selectBookingCardByStatus(AppStatus.ACCEPTED);

        for (BookingCard bookingCard : bookingCards) {
            if (bookingCard.getCheckOut() < getServerPresentTime().getTimeInMillis()) {
                bookingCard.setStatus(AppStatus.FINISHED.getType());

                bookingCardRepository.save(bookingCard);
            }
        }
    }
}
