package xao.develop.model;

import org.springframework.data.repository.CrudRepository;

public interface UserLanguageRepository extends CrudRepository<UserLanguage, Long> {
    UserLanguage getByChatId(Long chatId);
}
