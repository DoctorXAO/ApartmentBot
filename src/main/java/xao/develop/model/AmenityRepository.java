package xao.develop.model;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface AmenityRepository extends CrudRepository<Amenity, Integer> {
    @NotNull List<Amenity> findAll();

    Amenity getByIdAmenity(int idAmenity);
}
