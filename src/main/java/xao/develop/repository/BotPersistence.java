package xao.develop.repository;

import org.springframework.beans.factory.annotation.Autowired;
import xao.develop.model.TempUserMessage;
import xao.develop.model.TempUserMessagesRepository;
import xao.develop.model.UserLanguage;
import xao.develop.model.UserLanguageRepository;

import java.util.List;

public class BotPersistence {

    @Autowired
    private TempUserMessagesRepository tempUserMessagesRepository;

    @Autowired
    private UserLanguageRepository userLanguageRepository;

    public void insertUserLanguage(Long chatId, String language) {
        UserLanguage userLanguage = new UserLanguage();
        userLanguage.setChatId(chatId);
        userLanguage.setLanguage(language);

        userLanguageRepository.save(userLanguage);
    }

    public UserLanguage selectUserLanguage(Long chatId) {
        return userLanguageRepository.getByChatId(chatId);
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
