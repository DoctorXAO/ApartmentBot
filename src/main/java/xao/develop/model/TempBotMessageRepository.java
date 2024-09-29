package xao.develop.model;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface TempBotMessageRepository extends CrudRepository<TempBotMessage, Long> {
    @NotNull
    List<TempBotMessage> findAll();

    List<TempBotMessage> findByChatId(Long chatId);

    @Query("SELECT DISTINCT t.chatId FROM TempBotMessages t")
    List<Long> findDistinctChatIds();

    @Transactional
    void deleteByChatId(Long chatId);

    @Transactional
    void deleteByChatIdAndMsgId(Long chatId, Integer msgId);
}
