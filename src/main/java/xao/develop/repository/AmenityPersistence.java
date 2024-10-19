package xao.develop.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import xao.develop.model.Amenity;
import xao.develop.model.AmenityRepository;
import xao.develop.model.TempNewAmenity;
import xao.develop.toolbox.PropertiesManager;

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

    /** Select entity from Amenity by link **/
    public Amenity select(String link) {
        return amenityRepository.getByLink(link);
    }

    /** Update importance of amenity **/
    public void updateImportance(String link, int importance) {
        Amenity amenity = select(link);

        amenity.setImportance(importance);

        amenityRepository.save(amenity);
    }

    /** Delete amenity by id **/
    public void delete(int idOfAmenity) {
        amenityRepository.deleteById(idOfAmenity);

        log.debug("Amenity {} deleted successfully!", idOfAmenity);
    }


    public boolean isExist(String link) {
        List<Amenity> amenities = select();

        for(Amenity amenity : amenities) {
            log.debug("Amenity link: {}\nCheck link: {}\nIs equals: {}",
                    amenity.getLink(), link, amenity.getLink().equals(link));
            if (amenity.getLink().equals(link))
                return true;
        }

        return false;
    }

    public boolean isExist(int importance) {
        List<Amenity> amenities = select();

        for(Amenity amenity : amenities) {
            log.debug("Amenity importance: {}\nCheck importance: {}\nIs equals: {}",
                    amenity.getImportance(), importance, amenity.getImportance() == importance);
            if (amenity.getImportance() == importance)
                return true;
        }

        return false;
    }

    public Object[] getParameters(int idOfAmenity) {
        Amenity amenity = select(idOfAmenity);

        return new Object[]{
                amenity.getLink(),
                PropertiesManager.getPropertyValue("./config/languages/amenity.properties", amenity.getLink()),
                PropertiesManager.getPropertyValue("./config/languages/amenity_tr.properties", amenity.getLink()),
                PropertiesManager.getPropertyValue("./config/languages/amenity_ru.properties", amenity.getLink()),
                amenity.getImportance()
        };
    }
}
