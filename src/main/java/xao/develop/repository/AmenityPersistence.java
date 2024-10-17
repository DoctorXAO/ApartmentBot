package xao.develop.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import xao.develop.model.Amenity;
import xao.develop.model.AmenityRepository;

import java.util.List;

@Slf4j
@Repository
public class AmenityPersistence {

    @Autowired
    private final AmenityRepository amenityRepository;

    public AmenityPersistence(AmenityRepository amenityRepository) {
        this.amenityRepository = amenityRepository;
    }

    /** Insert an entity to Amenity **/
    public void insert(String link, int importance) {
        Amenity amenity = new Amenity();

        amenity.setLink(link);
        amenity.setImportance(importance);

        amenityRepository.save(amenity);
    }

    /** Select all entities from Amenity **/
    public List<Amenity> select() {
        return amenityRepository.findAll();
    }

    /** Select entity from Amenity by id **/
    public Amenity select(int idOfAmenity) {
        return amenityRepository.getByIdAmenity(idOfAmenity);
    }

    public Amenity select(String link) {
        return amenityRepository.getByLink(link);
    }

    public void updateImportance(String link, int importance) {
        Amenity amenity = select(link);

        amenity.setImportance(importance);

        amenityRepository.save(amenity);
    }

    public void delete(int idOfAmenity) {
        amenityRepository.deleteById(idOfAmenity);

        log.debug("Amenity {} deleted successfully!", idOfAmenity);
    }
}
