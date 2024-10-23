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
import xao.develop.service.BotService;
import xao.develop.service.admin.AdminMsgApplyDeleteAmenity;
import xao.develop.service.admin.operation.createAmenity.enums.AmenityStage;
import xao.develop.service.admin.operation.editAmenity.msg.AdminMsgEditAmenity;
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

    @Autowired
    BotService service;

    public void openEditAmenity(long chatId, List<Integer> messages, String data) throws TelegramApiException {
        adminMsgEditAmenity.updateSelectedAmenityAdminSettings(chatId, Integer.parseInt(data));
        adminMsgEditAmenity.updateEditingAmenity(chatId, true);

        messages.add(adminMsgEditAmenity.editMessage(chatId, ADMIN_MSG_EDIT_AMENITY,
                amenityPersistence.getParameters(adminMsgEditAmenity.getSelectedAmenity(chatId))));
    }

    public void openDeleteAmenity(long chatId, List<Integer> messages) throws TelegramApiException {
        Amenity amenity = amenityPersistence.select(adminMsgEditAmenity.getSelectedAmenity(chatId));

        messages.add(adminMsgApplyDeleteAmenity.editMessage(chatId, ADMIN_MSG_APPLY_DELETE_AMENITY, amenity.getLink()));
    }

    public void editParameters(long chatId,
                               String[] data,
                               List<Integer> messages,
                               AmenityStage stage) throws TelegramApiException {
        if (adminMsgEditAmenity.isEditingAmenity(chatId) && data.length > 1) {
            String link = amenityPersistence.select(persistence.selectAdminSettings(chatId).getSelectedAmenity()).getLink();

            switch (stage) {
                case EN -> editEn(link, data[1]);
                case TR -> editTr(link, data[1]);
                case UK -> editUk(link, data[1]);
                case RU -> editRu(link, data[1]);
            }

            if (stage.equals(AmenityStage.IMPORTANCE)) {
                editImportance(chatId, messages, link, data[1]);
            } else
                messages.add(adminMsgEditAmenity.editMessage(chatId, ADMIN_MSG_EDIT_AMENITY,
                        amenityPersistence.getParameters(adminMsgEditAmenity.getSelectedAmenity(chatId))));
        }
    }

    private void editEn(String link, String data) {
        PropertiesManager.addOrUpdateProperty("./config/languages/amenity.properties", link, data);
    }

    private void editTr(String link, String data) {
        PropertiesManager.addOrUpdateProperty("./config/languages/amenity_tr.properties", link, data);
    }

    private void editUk(String link, String data) {
        PropertiesManager.addOrUpdateProperty("./config/languages/amenity_uk.properties", link, data);
    }

    private void editRu(String link, String data) {
        PropertiesManager.addOrUpdateProperty("./config/languages/amenity_ru.properties", link, data);
    }

    private void editImportance(long chatId, List<Integer> messages, String link, String data) throws TelegramApiException {
        if (data.matches("[0-9]+")) {
            int importance = Integer.parseInt(data);

            if (amenityPersistence.isExist(importance))
                amenityPersistence.select().forEach(amenity -> {
                    if (amenity.getImportance() >= importance)
                        amenityPersistence.updateImportance(amenity.getLink(), amenity.getImportance() + 1);
                });

            amenityPersistence.updateImportance(link, Integer.parseInt(data));

            messages.add(adminMsgEditAmenity.editMessage(chatId, ADMIN_MSG_EDIT_AMENITY,
                    amenityPersistence.getParameters(adminMsgEditAmenity.getSelectedAmenity(chatId))));
        } else
            service.sendMessageInfo(chatId, ADMIN_ERR_EDIT_IMPORTANCE, adminMsgEditAmenity.getIKMarkupOkToDelete(chatId));
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
        PropertiesManager.removeProperty("./config/languages/amenity_uk.properties", amenity.getLink());
        PropertiesManager.removeProperty("./config/languages/amenity_ru.properties", amenity.getLink());

        messages.add(adminMsgListOfAmenities.editMessage(chatId, ADMIN_MSG_LIST_OF_AMENITIES,
                adminMsgListOfAmenities.getListOfAmenitiesPages(chatId)));
    }
}
