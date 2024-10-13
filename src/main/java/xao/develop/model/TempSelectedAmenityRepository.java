package xao.develop.model;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface TempSelectedAmenityRepository extends CrudRepository<TempSelectedAmenity, Integer> {
    @Query("SELECT sa.amenity FROM TempSelectedAmenities sa WHERE sa.chatId = chatId ORDER BY sa.amenity.importance ASC")
    List<Amenity> findAllByChatId(@Param("chatId") long chatId);

    @Transactional
    void deleteByChatIdAndAmenity(long chatId, Amenity amenity);

    @Transactional
    void deleteAllByChatId(long chatId);
}
