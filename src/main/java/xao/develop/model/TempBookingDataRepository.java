package xao.develop.model;

import org.springframework.data.repository.CrudRepository;

public interface TempBookingDataRepository extends CrudRepository<TempBookingData, Long> {
    TempBookingData getByChatId(Long chatId);
}
