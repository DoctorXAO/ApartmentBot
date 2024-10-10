package xao.develop.model;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface TempSelectedAmenityRepository extends CrudRepository<TempSelectedAmenity, Integer> {
    List<TempSelectedAmenity> findAllByChatId(long chatId);

    List<TempSelectedAmenity> findAllByChatIdAndIdOfAmenityNot(long chatId, int idOfAmenity);

    @Transactional
    void deleteByIdOfAmenity(int idOfAmenity);

    @Transactional
    void deleteAllByChatId(long chatId);
}
