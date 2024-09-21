package xao.develop.model;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ApartmentRepository extends CrudRepository<Apartment, Long> {
    List<Apartment> findAll();

    Apartment getByNumber(Long number);
}
