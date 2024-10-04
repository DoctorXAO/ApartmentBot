package xao.develop.model;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface BookingCardRepository extends CrudRepository<BookingCard, Long> {
    BookingCard findById(int id);

    List<BookingCard> findAllByStatus(String status);

    @Query("SELECT b FROM BookingCards b WHERE b.status <> 'waiting'")
    List<BookingCard> findAllExceptWaiting();
}
