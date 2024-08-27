package xao.develop.repository;

import xao.develop.model.TempUserMessage;
import xao.develop.model.UserStatus;

import java.util.List;

public interface User {

    boolean isUserStatusExists(Long chatId);
    void insertUserStatus(Long chatId,
                          String login,
                          String firstName,
                          String lastName,
                          String language,
                          Integer fillingOutStep);
    UserStatus selectUserStatus(Long chatId);
    void updateUserStatusLanguage(Long chatId, String language);
    void updateUserStatusFillingOutStep(Long chatId, Integer step);
    void updateUserStatusName(Long chatId, String name);
    void updateUserStatusCountOfPerson(Long chatId, String countOfPerson);
    void updateUserStatusRentTime(Long chatId, String rentTime);
    void updateUserStatusCommentary(Long chatId, String comment);

    List<TempUserMessage> selectUserMessages(Long chatID);
    void insertUserMessage(long chatId, int messageId);
    void deleteUserMessage(long chatId);
}
