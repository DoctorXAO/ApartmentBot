package xao.develop.model;

import org.springframework.data.repository.CrudRepository;

public interface TempNewApartmentRepository extends CrudRepository<TempNewApartment, Long> {
    TempNewApartment findByChatId(long chatId);
}
