package xao.develop.service.admin.operation.createAmenity.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xao.develop.model.Amenity;
import xao.develop.model.TempNewAmenity;
import xao.develop.repository.AmenityPersistence;
import xao.develop.repository.TempNewAmenityPersistence;
import xao.develop.toolbox.PropertiesManager;

import java.util.List;

@Slf4j
@Service
public class TempNewAmenityService {

    @Autowired
    AmenityPersistence amenityPersistence;

    @Autowired
    TempNewAmenityPersistence tempNewAmenityPersistence;


    public void insert(long chatId) {
        tempNewAmenityPersistence.insert(chatId);
    }

    public TempNewAmenity select(long chatId) {
        return tempNewAmenityPersistence.select(chatId);
    }

    public void updateLink(long chatId, String link) {
        tempNewAmenityPersistence.updateLink(chatId, "amenities." + link.toLowerCase());
    }

    public void updateEn(long chatId, String en) {
        tempNewAmenityPersistence.updateEn(chatId, en);
    }

    public void updateTr(long chatId, String tr) {
        tempNewAmenityPersistence.updateTr(chatId, tr);
    }

    public void updateRu(long chatId, String ru) {
        tempNewAmenityPersistence.updateRu(chatId, ru);
    }

    public void updateImportance(long chatId, int importance) {
        tempNewAmenityPersistence.updateImportance(chatId, importance);
    }

    public void delete(long chatId) {
        tempNewAmenityPersistence.delete(chatId);
    }

    public boolean isExist(String link) {
        List<Amenity> amenities = amenityPersistence.select();

        for(Amenity amenity : amenities) {
            log.debug("Amenity link: {}\nCheck link: {}\nIs equals: {}",
                    amenity.getLink(), link, amenity.getLink().equals(link));
            if (amenity.getLink().equals(link))
                return true;
        }

        return false;
    }

    public boolean isExist(int importance) {
        List<Amenity> amenities = amenityPersistence.select();

        for(Amenity amenity : amenities) {
            log.debug("Amenity importance: {}\nCheck importance: {}\nIs equals: {}",
                    amenity.getImportance(), importance, amenity.getImportance() == importance);
            if (amenity.getImportance() == importance)
                return true;
        }

        return false;
    }

    public Object[] getParameters(long chatId) {
        TempNewAmenity tempNewAmenity = select(chatId);

        return new Object[]{
                tempNewAmenity.getLink(),
                tempNewAmenity.getEn(),
                tempNewAmenity.getTr(),
                tempNewAmenity.getRu(),
                tempNewAmenity.getImportance()
        };
    }

    public void createAmenity(TempNewAmenity tempNewAmenity) {
        int importance = tempNewAmenity.getImportance();

        if (isExist(importance))
            amenityPersistence.select().forEach(amenity -> {
                if (amenity.getImportance() >= importance)
                    amenityPersistence.updateImportance(amenity.getLink(), amenity.getImportance() + 1);
            });

        PropertiesManager.addOrUpdateProperty("./config/languages/amenity.properties",
                tempNewAmenity.getLink(), tempNewAmenity.getEn());
        PropertiesManager.addOrUpdateProperty("./config/languages/amenity_tr.properties",
                tempNewAmenity.getLink(), tempNewAmenity.getTr());
        PropertiesManager.addOrUpdateProperty("./config/languages/amenity_ru.properties",
                tempNewAmenity.getLink(), tempNewAmenity.getRu());

        amenityPersistence.insert(tempNewAmenity.getLink(), tempNewAmenity.getImportance());
    }
}
