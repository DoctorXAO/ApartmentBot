package xao.develop.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;
import xao.develop.model.*;

import java.util.Calendar;
import java.util.List;

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
    private AmenityRepository amenityRepository;

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

    public List<TempBotMessage> selectTempBotMessages(Long chatID) {
        return tempBotMessageRepository.findByChatId(chatID);
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

    public void selectApartment(int number) {
        apartmentRepository.getByNumber(number);
    }

    public List<Apartment> selectAllApartments() {
        return apartmentRepository.findAll();
    }

    public void updateIsBookingApartment(int number, boolean isBooking) {
        Apartment apartment = apartmentRepository.getByNumber(number);

        apartment.setIsBooking(isBooking);

        apartmentRepository.save(apartment);
    }

    public void insertTempBookingData(long chatId, long selectedTime) {
        TempBookingData tempBookingData = new TempBookingData();

        tempBookingData.setChatId(chatId);
        tempBookingData.setSelectedTime(selectedTime);

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

    public Amenity selectAmenity(int idAmenity) {
        return amenityRepository.getByIdAmenity(idAmenity);
    }

    @Scheduled(cron = "0 0 0 * * ?")
    @EventListener(ContextRefreshedEvent.class)
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

    public Calendar getServerPresentTime() {
        ServerStatus serverStatus = serverStatusRepository.getByCode("MAIN");

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(serverStatus.getPresentTime());

        return calendar;
    }
}
