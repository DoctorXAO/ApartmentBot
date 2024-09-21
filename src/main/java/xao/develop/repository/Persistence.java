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

    public void selectApartment(Long number) {
        apartmentRepository.getByNumber(number);
    }

    public List<Apartment> selectAllApartments() {
        return apartmentRepository.findAll();
    }

    public void insertTempBookingData(long chatId, long selectedTime) {
        TempBookingData tempBookingData = new TempBookingData();

        tempBookingData.setChatId(chatId);
        tempBookingData.setSelectedTime(selectedTime);

        tempBookingDataRepository.save(tempBookingData);

        log.debug("Added new user to UserCalendar: {}", chatId);
    }

    public TempBookingData selectTempBookingData(long chatId) {
        log.debug("Select user from TempBookingDataRepository: {}", chatId);

        return tempBookingDataRepository.getByChatId(chatId);
    }

    public void updateTempBookingData(long chatId, long selectedTime) {
        log.trace("Method updateTempBookingData(long, long) started");

        TempBookingData tempBookingData = tempBookingDataRepository.findById(chatId).orElseThrow();

        log.debug("TempBookingData is updating for user: {}. Old value of selectedTime: {}", chatId, tempBookingData.getSelectedTime());

        tempBookingData.setSelectedTime(selectedTime);

        tempBookingDataRepository.save(tempBookingData);

        log.debug("TempBookingData updated for user: {}. New value of selectedTime: {}", chatId, tempBookingData.getSelectedTime());
        log.trace("Method updateTempBookingData(long, long) finished");
    }

    public void deleteTempBookingData(long chatId) {
        log.trace("Method deleteTempBookingData(long) started");

        tempBookingDataRepository.deleteById(chatId);

        log.debug("The next user deleted from TempBookingData: {}", chatId);
        log.trace("Method deleteTempBookingData(long) finished");
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
