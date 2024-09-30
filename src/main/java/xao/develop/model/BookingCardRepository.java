package xao.develop.model;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface BookingCardRepository extends CrudRepository<BookingCard, Long> {
    BookingCard getById(long id);

    List<BookingCard> findAllByStatus(String status);
}
