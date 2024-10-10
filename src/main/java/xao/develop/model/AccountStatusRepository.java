package xao.develop.model;

import org.springframework.data.repository.CrudRepository;

public interface AccountStatusRepository extends CrudRepository<AccountStatus, Long> {
    AccountStatus getByChatId(Long chatId);
}
