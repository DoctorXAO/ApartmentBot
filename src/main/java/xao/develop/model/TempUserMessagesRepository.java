package xao.develop.model;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface TempUserMessagesRepository extends CrudRepository<TempUserMessage, Long> {
    List<TempUserMessage> findByChatId(Long chatId);

    @Transactional
    void deleteByChatId(Long chatId);
}
