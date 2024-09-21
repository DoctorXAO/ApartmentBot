package xao.develop.model;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface TempBotMessageRepository extends CrudRepository<TempBotMessage, Long> {
    List<TempBotMessage> findByChatId(Long chatId);

    @Transactional
    void deleteByChatId(Long chatId);
}
