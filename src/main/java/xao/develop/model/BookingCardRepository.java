package xao.develop.model;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface BookingCardRepository extends CrudRepository<BookingCard, Long> {
    @NotNull Optional<BookingCard> findById(@NotNull Long id);

    List<BookingCard> findAllByStatus(String status);
}
