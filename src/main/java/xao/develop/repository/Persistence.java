package xao.develop.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import xao.develop.model.*;

import java.util.List;

@Repository
public class Persistence {

    @Autowired
    private TempUserMessagesRepository tempUserMessagesRepository;

    @Autowired
    private UserStatusRepository userStatusRepository;

    @Autowired
    private ApartmentsRepository apartmentsRepository;

    public boolean isUserStatusExists(Long chatId) {
        return userStatusRepository.existsByChatId(chatId);
    }

    public void insertUserStatus(Long chatId,
                                 String login,
                                 String firstName,
                                 String lastName,
                                 String language,
                                 Integer fillingOutStep) {
        UserStatus userStatus = new UserStatus();

        userStatus.setChatId(chatId);
        userStatus.setLogin(login);
        userStatus.setFirstName(firstName);
        userStatus.setLastName(lastName);
        userStatus.setLanguage(language);
        userStatus.setFillingOutStep(fillingOutStep);

        userStatusRepository.save(userStatus);
    }

    public UserStatus selectUserStatus(Long chatId) {
        return userStatusRepository.getByChatId(chatId);
    }

    /** Обновить язык интерфейса пользователя **/
    public void updateUserStatusLanguage(Long chatId, String language) {
        UserStatus userStatus = userStatusRepository.findById(chatId).orElseThrow();

        userStatus.setLanguage(language);

        userStatusRepository.save(userStatus);
    }

    /** Обновление этапа заполнения заявки **/
    public void updateUserStatusFillingOutStep(Long chatId, Integer step) {
        UserStatus userStatus = userStatusRepository.findById(chatId).orElseThrow();

        userStatus.setFillingOutStep(step);

        userStatusRepository.save(userStatus);
    }

    public void updateUserStatusName(Long chatId, String name) {
        UserStatus userStatus = userStatusRepository.findById(chatId).orElseThrow();

        userStatus.setName(name);

        userStatusRepository.save(userStatus);
    }

    public void updateUserStatusCountOfPerson(Long chatId, String countOfPerson) {
        UserStatus userStatus = userStatusRepository.findById(chatId).orElseThrow();

        userStatus.setCountOfPerson(countOfPerson);

        userStatusRepository.save(userStatus);
    }

    public void updateUserStatusRentTime(Long chatId, String rentTime) {
        UserStatus userStatus = userStatusRepository.findById(chatId).orElseThrow();

        userStatus.setRentTime(rentTime);

        userStatusRepository.save(userStatus);
    }

    public void updateUserStatusCommentary(Long chatId, String comment) {
        UserStatus userStatus = userStatusRepository.findById(chatId).orElseThrow();

        userStatus.setCommentary(comment);

        userStatusRepository.save(userStatus);
    }

    public List<TempUserMessage> selectUserMessages(Long chatID) {
        return tempUserMessagesRepository.findByChatId(chatID);
    }

    public void insertUserMessage(long chatId, int messageId) {
        TempUserMessage tempUserMessage = new TempUserMessage();
        tempUserMessage.setChatId(chatId);
        tempUserMessage.setMsgId(messageId);

        tempUserMessagesRepository.save(tempUserMessage);
    }

    public void deleteUserMessage(long chatId) {
        tempUserMessagesRepository.deleteByChatId(chatId);
    }

    public void selectApartment(Long number) {
        apartmentsRepository.getByNumber(number);
    }

    public List<Apartments> selectAllApartments() {
        return apartmentsRepository.findAll();
    }
}
