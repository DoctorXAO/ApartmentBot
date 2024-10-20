package xao.develop.service.admin.operation.createAmenity.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xao.develop.model.TempNewAmenity;
import xao.develop.repository.AmenityPersistence;
import xao.develop.repository.TempNewAmenityPersistence;
import xao.develop.toolbox.PropertiesManager;

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

    public void updateUk(long chatId, String uk) {
        tempNewAmenityPersistence.updateUk(chatId, uk);
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

    public Object[] getParameters(long chatId) {
        TempNewAmenity tempNewAmenity = select(chatId);

        return new Object[]{
                tempNewAmenity.getLink(),
                tempNewAmenity.getEn(),
                tempNewAmenity.getTr(),
                tempNewAmenity.getUk(),
                tempNewAmenity.getRu(),
                tempNewAmenity.getImportance()
        };
    }

    public void createAmenity(TempNewAmenity tempNewAmenity) {
        int importance = tempNewAmenity.getImportance();

        if (amenityPersistence.isExist(importance))
            amenityPersistence.select().forEach(amenity -> {
                if (amenity.getImportance() >= importance)
                    amenityPersistence.updateImportance(amenity.getLink(), amenity.getImportance() + 1);
            });

        PropertiesManager.addOrUpdateProperty("./config/languages/amenity.properties",
                tempNewAmenity.getLink(), tempNewAmenity.getEn());
        PropertiesManager.addOrUpdateProperty("./config/languages/amenity_tr.properties",
                tempNewAmenity.getLink(), tempNewAmenity.getTr());
        PropertiesManager.addOrUpdateProperty("./config/languages/amenity_uk.properties",
                tempNewAmenity.getLink(), tempNewAmenity.getUk());
        PropertiesManager.addOrUpdateProperty("./config/languages/amenity_ru.properties",
                tempNewAmenity.getLink(), tempNewAmenity.getRu());

        amenityPersistence.insert(tempNewAmenity.getLink(), tempNewAmenity.getImportance());
    }
}
