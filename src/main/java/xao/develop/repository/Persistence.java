package xao.develop.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import xao.develop.enums.UserStep;
import xao.develop.model.*;
import xao.develop.enums.AppStatus;

import java.util.Calendar;
import java.util.List;

@Slf4j
@Repository
public class Persistence {

    @Autowired
    private final ServerStatusRepository serverStatusRepository;

    @Autowired
    private final AccountStatusRepository accountStatusRepository;

    @Autowired
    private final ApartmentRepository apartmentRepository;

    @Autowired
    private final BookingCardRepository bookingCardRepository;

    @Autowired
    private final TempBotMessageRepository tempBotMessageRepository;

    @Autowired
    private final TempBookingDataRepository tempBookingDataRepository;

    @Autowired
    private final TempApartmentSelectorRepository tempApartmentSelectorRepository;

    @Autowired
    private final AdminSettingsRepository adminSettingsRepository;

    @Autowired
    private final TempNewApartmentRepository tempNewApartmentRepository;

    @Autowired
    private final TempSelectedAmenityRepository tempSelectedAmenityRepository;

    public Persistence(ServerStatusRepository serverStatusRepository,
                       AccountStatusRepository accountStatusRepository,
                       ApartmentRepository apartmentRepository,
                       BookingCardRepository bookingCardRepository,
                       TempBotMessageRepository tempBotMessageRepository,
                       TempBookingDataRepository tempBookingDataRepository,
                       TempApartmentSelectorRepository tempApartmentSelectorRepository,
                       AdminSettingsRepository adminSettingsRepository,
                       TempNewApartmentRepository tempNewApartmentRepository,
                       TempSelectedAmenityRepository tempSelectedAmenityRepository) {

        this.serverStatusRepository = serverStatusRepository;
        this.accountStatusRepository = accountStatusRepository;
        this.apartmentRepository = apartmentRepository;
        this.bookingCardRepository = bookingCardRepository;
        this.tempBotMessageRepository = tempBotMessageRepository;
        this.tempBookingDataRepository = tempBookingDataRepository;
        this.tempApartmentSelectorRepository = tempApartmentSelectorRepository;
        this.adminSettingsRepository = adminSettingsRepository;
        this.tempNewApartmentRepository = tempNewApartmentRepository;
        this.tempSelectedAmenityRepository = tempSelectedAmenityRepository;
    }

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

    public void updateNumberApartment(int number, int newNumber) {
        Apartment apartment = apartmentRepository.getByNumber(number);

        apartment.setNumber(newNumber);

        apartmentRepository.save(apartment);

        apartmentRepository.deleteByNumber(number);
    }

    public void updateAreaApartment(int number, double area) {
        Apartment apartment = apartmentRepository.getByNumber(number);

        apartment.setArea(area);

        apartmentRepository.save(apartment);
    }

    public void updateAmenitiesApartment(int number, List<Amenity> amenities) {
        Apartment apartment = apartmentRepository.getByNumber(number);

        apartment.setAmenities(amenities);

        apartmentRepository.save(apartment);
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

    public void deleteApartment(int numberOfApartment) {
        apartmentRepository.deleteByNumber(numberOfApartment);

        log.debug("Apartment №{} deleted", numberOfApartment);
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

    public void updateStepTempBookingData(long chatId, UserStep step) {
        log.trace("Method updateStepTempBookingData(long, UserStep) started");

        TempBookingData tempBookingData = tempBookingDataRepository.findById(chatId).orElseThrow();

        tempBookingData.setStep(step.getStep());

        tempBookingDataRepository.save(tempBookingData);

        log.trace("Method updateStepTempBookingData(long, UserStep) finished");
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

    // AdminSettings

    public void insertAdminSettings(long chatId) {
        AdminSettings adminSettings = new AdminSettings();

        adminSettings.setChatId(chatId);

        adminSettingsRepository.save(adminSettings);
    }

    public AdminSettings selectAdminSettings(long chatId) {
        return adminSettingsRepository.findById(chatId);
    }

    public void updateSelectedAppAdminSettings(long chatId, int selectedApp) {
        AdminSettings adminSettings;

        try {
            adminSettings = adminSettingsRepository.findById(chatId);

            adminSettings.setSelectedApplication(selectedApp);
        }
        catch (NullPointerException ex) {
            adminSettings = new AdminSettings();

            adminSettings.setChatId(chatId);
            adminSettings.setSelectedApplication(selectedApp);
        }

        adminSettingsRepository.save(adminSettings);
    }

    public void updateSelectedApartmentAdminSettings(long chatId, int selectedApartment) {
        AdminSettings adminSettings = adminSettingsRepository.findById(chatId);

        adminSettings.setSelectedApartment(selectedApartment);

        adminSettingsRepository.save(adminSettings);
    }

    public void updateSelectedAmenityAdminSettings(long chatId, int selectedAmenity) {
        AdminSettings adminSettings = adminSettingsRepository.findById(chatId);

        adminSettings.setSelectedAmenity(selectedAmenity);

        adminSettingsRepository.save(adminSettings);
    }

    public void updateSelectedPageAdminSettings(long chatId, int selectedPage) {
        AdminSettings adminSettings = adminSettingsRepository.findById(chatId);

        adminSettings.setSelectedPage(selectedPage);

        adminSettingsRepository.save(adminSettings);
    }

    public void updateNewApartmentAdminSettings(long chatId, boolean isNewApartment) {
        AdminSettings adminSettings = adminSettingsRepository.findById(chatId);

        adminSettings.setNewApartment(isNewApartment);

        adminSettingsRepository.save(adminSettings);
    }

    public void updateCheckingSelectedAmenitiesAdminSettings(long chatId, boolean isCheckingSelectedAmenities) {
        AdminSettings adminSettings = adminSettingsRepository.findById(chatId);

        adminSettings.setCheckingSelectedAmenities(isCheckingSelectedAmenities);

        adminSettingsRepository.save(adminSettings);
    }

    public void updateEditingApartmentAdminSettings(long chatId, boolean isEditingApartment) {
        AdminSettings adminSettings = adminSettingsRepository.findById(chatId);

        adminSettings.setEditingApartment(isEditingApartment);

        adminSettingsRepository.save(adminSettings);
    }

    public void updateEditingPhotosAdminSettings(long chatId, boolean isEditingPhotos) {
        AdminSettings adminSettings = adminSettingsRepository.findById(chatId);

        adminSettings.setEditingPhotos(isEditingPhotos);

        adminSettingsRepository.save(adminSettings);
    }

    public void updateEditingAmenityAdminSettings(long chatId, boolean isEditingAmenity) {
        AdminSettings adminSettings = adminSettingsRepository.findById(chatId);

        adminSettings.setEditingAmenity(isEditingAmenity);

        adminSettingsRepository.save(adminSettings);
    }

    public void resetToDefaultAdminSettings() {
        adminSettingsRepository.resetToDefault();
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

    public void insertTempSelectedAmenity(long chatId, Amenity amenity) {
        TempSelectedAmenity tempSelectedAmenity = new TempSelectedAmenity();

        tempSelectedAmenity.setChatId(chatId);
        tempSelectedAmenity.setAmenity(amenity);

        tempSelectedAmenityRepository.save(tempSelectedAmenity);
    }

    public List<Amenity> selectAllSelectedAmenities(long chatId) {
        return tempSelectedAmenityRepository.findAllByChatId(chatId);
    }

    public void deleteTempSelectedAmenity(long chatId, Amenity amenity) {
        tempSelectedAmenityRepository.deleteByChatIdAndAmenity(chatId, amenity);
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
