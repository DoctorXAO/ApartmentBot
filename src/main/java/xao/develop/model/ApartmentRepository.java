package xao.develop.model;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ApartmentRepository extends CrudRepository<Apartment, Long> {
    List<Apartment> findAll(Sort sort);

    Apartment getByNumber(int number);

    @Transactional
    void deleteByNumber(int number);

    @Modifying
    @Transactional
    @Query("UPDATE Apartments a SET a.isBooking = false, a.userId = null")
    void resetToDefault();
}
