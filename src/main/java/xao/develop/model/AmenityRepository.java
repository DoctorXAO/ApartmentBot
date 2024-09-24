package xao.develop.model;

import org.springframework.data.repository.CrudRepository;

public interface AmenityRepository extends CrudRepository<Amenity, Integer> {
    Amenity getByIdAmenity(int idAmenity);
}
