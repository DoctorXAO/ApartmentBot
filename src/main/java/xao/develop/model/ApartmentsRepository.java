package xao.develop.model;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ApartmentsRepository extends CrudRepository<Apartments, Long> {
    List<Apartments> findAll();

    Apartments getByNumber(Long number);
}
