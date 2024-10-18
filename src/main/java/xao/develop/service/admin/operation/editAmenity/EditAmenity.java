package xao.develop.service.admin.operation.editAmenity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import xao.develop.command.AdminCommand;
import xao.develop.command.AdminMessageLink;
import xao.develop.model.Amenity;
import xao.develop.model.Apartment;
import xao.develop.repository.AmenityPersistence;
import xao.develop.repository.Persistence;
import xao.develop.service.admin.AdminMsgApplyDeleteAmenity;
import xao.develop.service.admin.AdminMsgEditAmenity;
import xao.develop.service.admin.AdminMsgListOfAmenities;
import xao.develop.toolbox.PropertiesManager;

import java.util.List;

@Service
public class EditAmenity implements AdminMessageLink, AdminCommand {

    @Autowired
    Persistence persistence;

    @Autowired
    AmenityPersistence amenityPersistence;

    @Autowired
    AdminMsgEditAmenity adminMsgEditAmenity;

    @Autowired
    AdminMsgApplyDeleteAmenity adminMsgApplyDeleteAmenity;

    @Autowired
    AdminMsgListOfAmenities adminMsgListOfAmenities;

    public void openEditAmenity(long chatId, List<Integer> messages, String data) throws TelegramApiException {
        adminMsgEditAmenity.updateSelectedAmenityAdminSettings(chatId, Integer.parseInt(data));

        messages.add(adminMsgEditAmenity.editMessage(chatId, ADMIN_MSG_EDIT_AMENITY));
    }

    public void openDeleteAmenity(long chatId, List<Integer> messages) throws TelegramApiException {
        Amenity amenity = amenityPersistence.select(adminMsgEditAmenity.getSelectedAmenity(chatId));

        messages.add(adminMsgApplyDeleteAmenity.editMessage(chatId, ADMIN_MSG_APPLY_DELETE_AMENITY, amenity.getLink()));
    }

    @Transactional
    public void deleteAmenity(long chatId, List<Integer> messages) throws TelegramApiException {
        Amenity amenity = amenityPersistence.select(adminMsgEditAmenity.getSelectedAmenity(chatId));

        List<Apartment> apartments = persistence.selectAllApartmentsSortByNumber();

        for(Apartment apartment : apartments)
            apartment.getAmenities().remove(amenity);

        amenityPersistence.delete(amenity.getIdAmenity());

        PropertiesManager.removeProperty("./config/languages/amenity.properties", amenity.getLink());
        PropertiesManager.removeProperty("./config/languages/amenity_tr.properties", amenity.getLink());
        PropertiesManager.removeProperty("./config/languages/amenity_ru.properties", amenity.getLink());

        messages.add(adminMsgListOfAmenities.editMessage(chatId, ADMIN_MSG_LIST_OF_AMENITIES));
    }
}
