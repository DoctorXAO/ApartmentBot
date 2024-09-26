package xao.develop.model;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

public interface TempApartmentSelectorRepository extends CrudRepository<TempApartmentSelector, Integer> {
    TempApartmentSelector findByChatId(long chatId);

    @Transactional
    void deleteByChatId(long chatId);
}
