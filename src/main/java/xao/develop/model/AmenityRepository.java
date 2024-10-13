package xao.develop.model;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface AmenityRepository extends CrudRepository<Amenity, Integer> {
    @Query("SELECT a FROM Amenities a ORDER BY a.importance ASC")
    @NotNull List<Amenity> findAll();

    Amenity getByIdAmenity(int idAmenity);
}
