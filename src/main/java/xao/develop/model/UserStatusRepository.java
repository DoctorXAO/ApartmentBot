package xao.develop.model;

import org.springframework.data.repository.CrudRepository;

public interface UserStatusRepository extends CrudRepository<UserStatus, Long> {
    UserStatus getByChatId(Long chatId);
}
