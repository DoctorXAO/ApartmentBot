package xao.develop.service.admin.operation.createAmenity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import xao.develop.command.AdminCommand;
import xao.develop.command.AdminMessageLink;
import xao.develop.repository.AmenityPersistence;
import xao.develop.service.BotService;
import xao.develop.service.admin.AdminMsgNewAmenity;
import xao.develop.service.admin.AdminMsgSettings;
import xao.develop.service.admin.operation.createAmenity.enums.AmenityStage;
import xao.develop.service.admin.operation.createAmenity.service.TempNewAmenityService;

import java.util.List;

@Service
public class CreateAmenity implements AdminMessageLink, AdminCommand {

    @Autowired
    BotService service;

    @Autowired
    TempNewAmenityService tempNewAmenityService;

    @Autowired
    AmenityPersistence amenityPersistence;

    @Autowired
    AdminMsgSettings adminMsgSettings;

    @Autowired
    AdminMsgNewAmenity adminMsgNewAmenity;

    public void openNewAmenity(long chatId,
                               List<Integer> messages,
                               String[] data,
                               AmenityStage stage,
                               boolean isInit) throws TelegramApiException {
        if (isInit)
            tempNewAmenityService.insert(chatId);

        if (data.length > 1)
            switch (stage) {
                case LINK -> setLink(chatId, messages, data[1]);
                case EN -> setEn(chatId, messages, data[1]);
                case TR -> setTr(chatId, messages, data[1]);
                case RU -> setRu(chatId, messages, data[1]);
                case IMPORTANCE -> setImportance(chatId, messages, data[1]);
            }
        else {
            Object[] parameters = tempNewAmenityService.getParameters(chatId);

            switch (stage) {
                case LINK -> messages.add(adminMsgNewAmenity.editMessage(chatId, ADMIN_MSG_SET_LINK, parameters));
                case EN -> messages.add(adminMsgNewAmenity.editMessage(chatId, ADMIN_MSG_SET_NAME_EN, tempNewAmenityService.getParameters(chatId)));
                case TR -> messages.add(adminMsgNewAmenity.editMessage(chatId, ADMIN_MSG_SET_NAME_TR, tempNewAmenityService.getParameters(chatId)));
                case RU -> messages.add(adminMsgNewAmenity.editMessage(chatId, ADMIN_MSG_SET_NAME_RU, tempNewAmenityService.getParameters(chatId)));
                case IMPORTANCE -> messages.add(adminMsgNewAmenity.editMessage(chatId, ADMIN_MSG_SET_IMPORTANCE, parameters));
            }
        }
    }

    private void setLink(long chatId, List<Integer> messages, String link) throws TelegramApiException {
        link = link.toLowerCase();

        if (amenityPersistence.isExist("amenities." + link))
            service.sendMessageInfo(chatId, ADMIN_ERR_LINK_OF_AMENITY_IS_EXIST,
                    adminMsgNewAmenity.getIKMarkupOkToDelete(chatId), link);
        else if (link.matches("[a-zA-Z-]+")) {
            tempNewAmenityService.updateLink(chatId, link);
            messages.add(adminMsgNewAmenity.editMessage(chatId, ADMIN_MSG_SET_NAME_EN,
                    tempNewAmenityService.getParameters(chatId)));
        } else
            service.sendMessageInfo(chatId, ADMIN_ERR_SET_LINK, adminMsgNewAmenity.getIKMarkupOkToDelete(chatId));
    }

    private void setEn(long chatId, List<Integer> messages, String en) throws TelegramApiException {
        tempNewAmenityService.updateEn(chatId, en);
        messages.add(adminMsgNewAmenity.editMessage(chatId, ADMIN_MSG_SET_NAME_TR,
                tempNewAmenityService.getParameters(chatId)));
    }

    private void setTr(long chatId, List<Integer> messages, String tr) throws TelegramApiException {
        tempNewAmenityService.updateTr(chatId, tr);
        messages.add(adminMsgNewAmenity.editMessage(chatId, ADMIN_MSG_SET_NAME_RU,
                tempNewAmenityService.getParameters(chatId)));
    }

    private void setRu(long chatId, List<Integer> messages, String ru) throws TelegramApiException {
        tempNewAmenityService.updateRu(chatId, ru);
        messages.add(adminMsgNewAmenity.editMessage(chatId, ADMIN_MSG_SET_IMPORTANCE,
                tempNewAmenityService.getParameters(chatId)));
    }

    private void setImportance(long chatId, List<Integer> messages, String importance) throws TelegramApiException {
        if (importance.matches("[0-9]+")) {
            tempNewAmenityService.updateImportance(chatId, Integer.parseInt(importance));
            messages.add(adminMsgNewAmenity.editMessage(chatId, ADMIN_MSG_AMENITY_VIEW, tempNewAmenityService.getParameters(chatId)));
        } else
            service.sendMessageInfo(chatId, ADMIN_ERR_SET_IMPORTANCE, adminMsgNewAmenity.getIKMarkupOkToDelete(chatId));
    }

    public void createNewAmenity(long chatId, List<Integer> messages) throws TelegramApiException {
        tempNewAmenityService.createAmenity(tempNewAmenityService.select(chatId));

        tempNewAmenityService.delete(chatId);

        messages.add(adminMsgSettings.editMessage(chatId, ADMIN_MSG_SETTINGS));

        service.sendTempMessage(chatId, ADMIN_MSG_SIMPLE_AMENITY_CREATED, 5);
    }

    public void quitFromNewAmenity(long chatId, List<Integer> messages) throws TelegramApiException {
        tempNewAmenityService.delete(chatId);
        messages.add(adminMsgSettings.editMessage(chatId, ADMIN_MSG_SETTINGS));
    }

    // others

    public void clearTempDAO(long chatId) {
        tempNewAmenityService.delete(chatId);
    }
}
