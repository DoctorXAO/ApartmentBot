package xao.develop.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import xao.develop.model.TempUserMessage;
import xao.develop.model.TempUserMessagesRepository;
import xao.develop.model.UserStatus;
import xao.develop.model.UserStatusRepository;

import java.util.List;

@Repository
public class UserPersistence {

    @Autowired
    private TempUserMessagesRepository tempUserMessagesRepository;

    @Autowired
    private UserStatusRepository userStatusRepository;

    public void insertUserStatus(Long chatId,
                                 String login,
                                 String firstName,
                                 String lastName,
                                 String language,
                                 Boolean isFillingOut) {
        UserStatus userStatus = new UserStatus();
        userStatus.setChatId(chatId);
        userStatus.setLogin(login);
        userStatus.setFirstName(firstName);
        userStatus.setLastName(lastName);
        userStatus.setLanguage(language);
        userStatus.setIsFillingOutAnApplication(isFillingOut);

        userStatusRepository.save(userStatus);
    }

    /** Обновить язык интерфейса пользователя **/
    public void updateUserLanguage(Long chatId, String language) {
        UserStatus userStatus = new UserStatus();
        userStatus.setChatId(chatId);
        userStatus.setLanguage(language);

        userStatusRepository.save(userStatus);
    }

    /** Обновить режим заполнения заявки пользователем **/
    public void updateUserIsFillingOut(Long chatId, Boolean isFillingOut) {
        UserStatus userStatus = new UserStatus();
        userStatus.setChatId(chatId);
        userStatus.setIsFillingOutAnApplication(isFillingOut);

        userStatusRepository.save(userStatus);
    }

    public UserStatus selectUserStatus(Long chatId) {
        return userStatusRepository.getByChatId(chatId);
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
}
