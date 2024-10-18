package xao.develop.service.admin;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import xao.develop.command.AdminCommand;
import xao.develop.command.AdminMessageLink;
import xao.develop.command.GeneralCommand;
import xao.develop.command.GeneralMessageLink;
import xao.develop.config.*;
import xao.develop.enums.*;
import xao.develop.model.Amenity;
import xao.develop.model.TempNewApartment;
import xao.develop.service.BotService;
import xao.develop.service.admin.operation.createAmenity.CreateAmenity;
import xao.develop.service.admin.operation.createAmenity.enums.AmenityStage;
import xao.develop.service.admin.operation.editAmenity.EditAmenity;
import xao.develop.toolbox.FileManager;
import xao.develop.toolbox.TelegramFileManager;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;

@Slf4j
@Service
public class AdminService implements GeneralCommand, GeneralMessageLink, AdminCommand, AdminMessageLink {

    @Autowired
    BotConfig botConfig;

    @Autowired
    BotService service;

    @Autowired
    CreateAmenity createAmenity;

    @Autowired
    EditAmenity editAmenity;

    @Autowired
    AdminMsgStart adminMsgStart;

    @Autowired
    AdminMsgNewApplications adminMsgNewApplications;

    @Autowired
    AdminMsgOpenApp adminMsgOpenApp;

    @Autowired
    AdminMsgArchive adminMsgArchive;

    @Autowired
    AdminMsgOpenArc adminMsgOpenArc;

    @Autowired
    AdminMsgSettings adminMsgSettings;

    @Autowired
    AdminMsgNewApartment adminMsgNewApartment;

    @Autowired
    AdminMsgListOfApartments adminMsgListOfApartments;

    @Autowired
    AdminMsgListOfAmenities adminMsgListOfAmenities;

    @Autowired
    AdminMsgChangeLanguage adminMsgChangeLanguage;

    @Autowired
    AdminMsgChat adminMsgChat;

    @Autowired
    AdminMsgNAAmenities adminMsgNAAmenities;

    @Autowired
    AdminMsgPreviewApartment adminMsgPreviewApartment;

    @Autowired
    AdminMsgEditApartment adminMsgEditApartment;

    @Autowired
    AdminMsgEditPhotos adminMsgEditPhotos;

    @Autowired
    AdminMsgEditNumber adminMsgEditNumber;

    @Autowired
    AdminMsgEditArea adminMsgEditArea;

    @Autowired
    AdminMsgEditAmenities adminMsgEditAmenities;

    @Autowired
    AdminMsgApplyDeleteApartment adminMsgApplyDeleteApartment;

    private final String tempFolderOfPhotos = "temp";

    public void execute(Update update) {
        log.trace("Method execute(Update, String) started");

        String signal = service.getData(update);

        String[] data;

        if (signal != null)
            data = service.getData(update).split(X, 2);
        else
            data = new String[]{EMPTY};

        for (String d : data)
            log.debug("data: {}", d);

        List<Integer> messages = new ArrayList<>();

        long chatId = service.getChatId(update);
        int msgId = service.getMessageId(update);
        User user = service.getUser(update);
        Message message = update.getMessage();

        try {
            if (update.hasMessage() && update.getMessage().hasPhoto())
                processingPhoto(chatId, msgId, message, messages, data);
            else if (update.hasMessage())
                processingMessage(chatId, msgId, user, messages, data);
            else if (update.hasCallbackQuery())
                processingCallbackQuery(chatId, msgId, user, messages, data);
            else
                log.warn("Unknown data: {}", data[0]);

            for (Integer messageId : messages)
                service.registerMessage(chatId, messageId);
        } catch (TelegramApiException ex) {
            log.error("Can't execute function: {}", ex.getMessage());
        }

        log.trace("Method execute(Update, String) finished");
    }

    public void dropData(long chatId) {
        adminMsgStart.createAdminSettings(chatId);
        adminMsgStart.deleteTempNewApartment(chatId);
        adminMsgStart.deleteSelectedAmenities(chatId);
        createAmenity.clearTempDAO(chatId);

        log.debug("Data dropped successfully for user {}!", chatId);
    }

    private void processingPhoto(long chatId,
                                 int msgId,
                                 Message message,
                                 List<Integer> messages,
                                 String[] data) throws TelegramApiException {

        List<PhotoSize> photos = message.getPhoto();

        String fileId = photos.get(photos.size() - 1).getFileId();
        String filePath = TelegramFileManager.getFilePath(botConfig.getTelegramClient(), fileId);

        String fileUrl = "https://api.telegram.org/file/bot" + botConfig.getToken() + "/" + filePath;

        if (service.getApartmentPath() != null) {
            String path = service.getApartmentPath() + tempFolderOfPhotos;

            if (adminMsgStart.isNewApartment(chatId)) {
                FileManager.downloadPhotoFromNetwork(path, fileUrl);

                adminMsgStart.updateCountOfPicturesTempNewApartment(chatId, FileManager.getCountOfFiles(Paths.get(path)));

                newApartment(chatId, messages, data, NewApartment.AREA);
            }
            else if (adminMsgStart.isEditingPhotos(chatId))
                FileManager.downloadPhotoFromNetwork(path, fileUrl);
        } else
            service.sendMessageInfo(chatId, ADMIN_ERR_DOWNLOAD_RESOURCE, adminMsgStart.getIKMarkupOkToDelete(chatId));

        deleteMessage(chatId, msgId);
    }

    private void processingMessage(long chatId,
                                   int msgId,
                                   User user,
                                   List<Integer> messages,
                                   String[] data) throws TelegramApiException {
        switch (data[0]) {
            case START -> start(chatId, user, messages, true, false);
            case CHAT -> chat(chatId, data);

            // Create apartment

            case NUMBER -> newApartment(chatId, messages, data, NewApartment.NUMBER);
            case PHOTOS -> newApartment(chatId, messages, data, NewApartment.PHOTOS);
            case AREA -> newApartment(chatId, messages, data, NewApartment.AREA);
            case AMENITIES -> newApartment(chatId, messages, data, NewApartment.AMENITIES);

            // Edit apartment

            case SET_NUMBER -> editNumber(chatId, messages, data);
            case SET_AREA -> editArea(chatId, messages, data);

            // Create amenity

            case SET_LINK -> createAmenity.openNewAmenity(chatId, messages, data, AmenityStage.LINK, false);
            case SET_NAME_EN -> createAmenity.openNewAmenity(chatId, messages, data, AmenityStage.EN, false);
            case SET_NAME_TR -> createAmenity.openNewAmenity(chatId, messages, data, AmenityStage.TR, false);
            case SET_NAME_RU -> createAmenity.openNewAmenity(chatId, messages, data, AmenityStage.RU, false);
            case SET_IMPORTANCE -> createAmenity.openNewAmenity(chatId, messages, data, AmenityStage.IMPORTANCE, false);

            default -> log.info("Unknown message data: {}", data[0]);
        }

        deleteMessage(chatId, msgId);
    }

    private void processingCallbackQuery(long chatId,
                                         int msgId,
                                         User user,
                                         List<Integer> messages,
                                         String[] data) throws TelegramApiException {
        switch (data[0]) {
            case NEW_APPLICATIONS, QUIT_FROM_APP -> openListOfApps(chatId, messages, App.APP);
            case ARCHIVE, QUIT_FROM_ARC -> openListOfApps(chatId, messages, App.ARC);
            case SETTINGS -> openSettings(chatId, messages, false);
            case CHANGE_LANGUAGE -> openChangeLanguage(chatId, messages);

            case OPEN_NEW_APARTMENT -> openNewApartment(chatId, messages, ADMIN_MSG_SET_NUMBER, true, true);
            case BACK_TO_EDIT_NEW_APARTMENT -> openNewApartment(chatId, messages, ADMIN_MSG_SET_NUMBER, false, false);
            case LIST_OF_APARTMENTS -> openListOfApartments(chatId, messages);
            case LIST_OF_AMENITIES -> openListOfAmenities(chatId, messages);

            case PREVIOUS_PAGE_OF_NEW_APPS -> changePage(chatId, messages, Page.APP, Selector.PREVIOUS);
            case NEXT_PAGE_OF_NEW_APPS -> changePage(chatId, messages, Page.APP, Selector.NEXT);
            case PREVIOUS_PAGE_OF_ARCHIVE -> changePage(chatId, messages, Page.ARC, Selector.PREVIOUS);
            case NEXT_PAGE_OF_ARCHIVE -> changePage(chatId, messages, Page.ARC, Selector.NEXT);
            case PREVIOUS_PAGE_OF_APART -> changePage(chatId, messages, Page.APR, Selector.PREVIOUS);
            case NEXT_PAGE_OF_APART -> changePage(chatId, messages, Page.APR, Selector.NEXT);

            case CREATE_APARTMENT -> createApartment(chatId, messages);
            case AVAILABLE -> changeListOfAmenities(chatId, messages, false);
            case SELECTED -> changeListOfAmenities(chatId, messages, true);
            case PREVIEW_APARTMENT -> previewApartment(chatId, messages);
            
            case APPLY_DELETE_APARTMENT -> applyDeleteApartment(chatId, messages);
            case DELETE_APARTMENT -> deleteApartment(chatId, messages);

            case APP -> openApp(chatId, messages, data[1], App.APP);
            case ARC -> openApp(chatId, messages, data[1], App.ARC);
            case AMENITY -> selectAmenity(chatId, messages, data[1]);
            case APARTMENT -> editApartment(chatId, messages, data[1]);

            case OPEN_EDIT_NUMBER -> openEditNumber(chatId, messages);
            case OPEN_EDIT_PHOTOS -> openEditPhotos(chatId, messages);
            case ADD_PHOTOS -> addPhotos(chatId, messages);
            case REPLACE_PHOTOS -> replacePhotos(chatId, messages);
            case OPEN_EDIT_AREA -> openEditArea(chatId, messages);
            case OPEN_EDIT_AMENITIES -> openEditAmenities(chatId, messages, true);
            case EDIT_AMENITIES -> editAmenities(chatId, messages, adminMsgStart.getSelectedApartment(chatId));

            case REFUSE_APP -> performActionStatement(chatId, messages, data[1], AppStatus.DENIED);
            case ACCEPT_APP -> performActionStatement(chatId, messages, data[1], AppStatus.ACCEPTED);
            case RETURN_APP -> performActionStatement(chatId, messages, data[1], AppStatus.WAITING);

            case OPEN_CHAT -> openChat(chatId, messages, data[1]);

            case QUIT_TO_SETTINGS -> openSettings(chatId, messages, true);

            // Create amenity

            case OPEN_NEW_AMENITY -> createAmenity.openNewAmenity(chatId, messages, data, AmenityStage.LINK, true);
            case CREATE_NEW_AMENITY -> createAmenity.createNewAmenity(chatId, messages);
            case QUIT_FROM_NEW_AMENITY -> createAmenity.quitFromNewAmenity(chatId, messages);

            // Edit amenity

            case ED_AMENITY -> editAmenity.openEditAmenity(chatId, messages, data[1]);
            case APPLY_DELETE_AMENITY -> editAmenity.openDeleteAmenity(chatId, messages);
            case DELETE_AMENITY -> editAmenity.deleteAmenity(chatId, messages);

            // Others

            case TR, EN, RU -> changeLanguage(chatId, user, messages, data[0]);

            case BACK_TO_START -> start(chatId, user, messages, false, true);

            case DELETE -> deleteMessage(chatId, msgId);

            default -> log.warn("Unknown callback query: {}", data[0]);
        }
    }


    private void start(long chatId,
                       User user,
                       List<Integer> messages,
                       boolean isInit,
                       boolean isBack) throws TelegramApiException {
        if (isInit) {
            service.authorization(chatId, user);
            dropData(chatId);
        }

        if (isBack)
            dropData(chatId);
        else
            adminMsgStart.createAdminSettings(chatId);

        messages.add(adminMsgStart.editMessage(chatId, ADMIN_MSG_START, user.getFirstName()));
    }

    private void chat(long chatId, String[] data) throws TelegramApiException {
        if (data.length > 1) {
            long userId = adminMsgStart.getUserId(adminMsgStart.getSelectedApp(chatId));

            service.sendMessageInfo(userId, ADMIN_MSG_CHATTING_ADMIN, adminMsgStart.getIKMarkupChat(userId), data[1]);

            service.sendMessageInfo(chatId, ADMIN_MSG_SENT_SUCCESSFULLY, adminMsgStart.getIKMarkupChat(chatId), data[1]);
        }
    }

    private void newApartment(long chatId,
                              List<Integer> messages,
                              String[] data,
                              NewApartment stage) throws TelegramApiException {
        if (adminMsgStart.isNewApartment(chatId)) {
            if (data.length == 2) {
                String parameter = data[1];

                switch (stage) {
                    case NUMBER -> {
                        if (service.getApartmentPath() == null)
                            service.sendMessageInfo(chatId, ADMIN_ERR_DOWNLOAD_RESOURCE,
                                    adminMsgStart.getIKMarkupOkToDelete(chatId));
                        else if (new File(service.getApartmentPath() + parameter).exists())
                            service.sendMessageInfo(chatId, ADMIN_ERR_APARTMENT_EXISTS,
                                    adminMsgStart.getIKMarkupOkToDelete(chatId));
                        else if (parameter.matches("[0-9]+")) {
                            adminMsgStart.updateNumberTempNewApartment(chatId, Integer.parseInt(parameter));
                            openNewApartment(chatId, messages, ADMIN_MSG_SET_PICTURES, false, false);
                        }
                    }
                    case AREA -> {
                        if (parameter.matches("[0-9.]+")) {
                            adminMsgStart.updateAreaTempNewApartment(chatId, Double.parseDouble(parameter));
                            openSelectAmenities(chatId, messages);
                        }
                    }
                }
            } else {
                switch (stage) {
                    case NUMBER -> openNewApartment(chatId, messages, ADMIN_MSG_SET_NUMBER, false, false);
                    case PHOTOS -> openNewApartment(chatId, messages, ADMIN_MSG_SET_PICTURES, false, false);
                    case AREA -> openNewApartment(chatId, messages, ADMIN_MSG_SET_AREA, false, false);
                    case AMENITIES -> openSelectAmenities(chatId, messages);
                }
            }
        }
    }

    private void openListOfApps(long chatId, List<Integer> messages, App type) throws TelegramApiException {
        if (type.equals(App.APP))
            messages.add(adminMsgNewApplications.editMessage(chatId, ADMIN_MSG_NEW_APPS, adminMsgStart.getCountOfNewApps()));
        else if (type.equals(App.ARC))
            messages.add(adminMsgArchive.editMessage(chatId, ADMIN_MSG_ARCHIVE, adminMsgStart.getCountOfArchive()));
    }

    private void openSettings(long chatId, List<Integer> messages, boolean isBack) throws TelegramApiException {
        if (isBack)
            dropData(chatId);

        messages.add(adminMsgSettings.editMessage(chatId, ADMIN_MSG_SETTINGS));
    }

    private void openChangeLanguage(long chatId, List<Integer> messages) throws TelegramApiException {
        messages.add(adminMsgChangeLanguage.editMessage(chatId, GENERAL_MSG_CHANGE_LANGUAGE));
    }

    private void openNewApartment(long chatId,
                                  List<Integer> messages,
                                  String msgLink,
                                  boolean isInit,
                                  boolean clearTemp) throws TelegramApiException {
        if (isInit) {
            adminMsgStart.updateNewApartmentAdminSettings(chatId, true);
            adminMsgStart.createNewApartmentField(chatId);
        }

        if (clearTemp && service.getApartmentPath() != null)
            FileManager.deleteDirectory(Paths.get(service.getApartmentPath() + tempFolderOfPhotos));
        else if (service.getApartmentPath() == null)
            service.sendMessageInfo(chatId, ADMIN_ERR_DOWNLOAD_RESOURCE, adminMsgStart.getIKMarkupOkToDelete(chatId));

        messages.add(adminMsgNewApartment.editMessage(chatId, msgLink, adminMsgArchive.getTempNewApartmentParameters(chatId)));
    }

    private void openSelectAmenities(long chatId, List<Integer> messages) throws TelegramApiException {
        messages.add(adminMsgNAAmenities.editMessage(chatId, ADMIN_MSG_SET_AMENITIES,
                adminMsgArchive.getTempNewApartmentParameters(chatId)));
    }

    private void openListOfApartments(long chatId, List<Integer> messages) throws TelegramApiException {
        adminMsgStart.updateSelectedApartment(chatId, 0);
        adminMsgStart.updateEditingApartment(chatId, false);
        adminMsgStart.updateEditingPhotos(chatId, false);
        
        messages.add(adminMsgListOfApartments.editMessage(chatId, ADMIN_MSG_LIST_OF_APARTMENTS));
    }

    private void openListOfAmenities(long chatId, List<Integer> messages) throws TelegramApiException {
        messages.add(adminMsgListOfAmenities.editMessage(chatId, ADMIN_MSG_LIST_OF_AMENITIES));
    }

    private void changePage(long chatId,
                            List<Integer> messages,
                            Page typeOfPage,
                            Selector typeOfSelector) throws TelegramApiException {

        switch (typeOfSelector) {
            case PREVIOUS -> adminMsgStart.previousPage(chatId);
            case NEXT -> adminMsgStart.nextPage(chatId);
        }

        switch (typeOfPage) {
            case APP -> messages.add(adminMsgNewApplications.editMessage(chatId, ADMIN_MSG_NEW_APPS,
                    adminMsgStart.getCountOfNewApps()));
            case ARC -> messages.add(adminMsgArchive.editMessage(chatId, ADMIN_MSG_ARCHIVE,
                    adminMsgStart.getCountOfArchive()));
            case APR -> messages.add(adminMsgSettings.editMessage(chatId, ADMIN_MSG_SETTINGS));
        }
    }

    private void createApartment(long chatId, List<Integer> messages) throws TelegramApiException {
        if (service.getApartmentPath() != null) {
            TempNewApartment newApartment = adminMsgStart.getTempNewApartment(chatId);
            List<Amenity> selectedAmenities = adminMsgStart.getSelectedAmenities(chatId);

            int number = newApartment.getNumber();
            double area = newApartment.getArea();

            String nameFolder = String.valueOf(number);

            FileManager.moveFiles(
                    Paths.get(service.getApartmentPath() + tempFolderOfPhotos + "/"),
                    Paths.get(service.getApartmentPath() + nameFolder + "/"));

            adminMsgStart.insertApartment(number, area, selectedAmenities);

            openSettings(chatId, messages, true);
            
            service.sendTempMessage(chatId, ADMIN_MSG_SIMPLE_NEW_APARTMENT_CREATED, 5);
        } else
            service.sendMessageInfo(chatId, ADMIN_ERR_DOWNLOAD_RESOURCE, adminMsgStart.getIKMarkupOkToDelete(chatId));
    }

    private void changeListOfAmenities(long chatId,
                                       List<Integer> messages,
                                       boolean isCheckingSelectedAmenities) throws TelegramApiException {
        adminMsgStart.updateCheckingSelectedAmenitiesAdminSettings(chatId, isCheckingSelectedAmenities);

        if (!adminMsgStart.isEditingApartments(chatId))
            openSelectAmenities(chatId, messages);
        else
            openEditAmenities(chatId, messages, false);
    }

    private void previewApartment(long chatId, List<Integer> messages) throws TelegramApiException {
        int msgId = service.sendSimpleMessage(chatId, GENERAL_MSG_SIMPLE_DOWNLOADING);

        messages.addAll(adminMsgPreviewApartment.sendPhotos(chatId, service.getApartmentPath() + tempFolderOfPhotos));

        messages.add(adminMsgPreviewApartment.sendMessage(chatId, ADMIN_MSG_PREVIEW_APARTMENT,
                adminMsgStart.getPreviewApartmentParameters(chatId)));

        deleteMessage(chatId, msgId);
    }

    private void applyDeleteApartment(long chatId, List<Integer> messages) throws TelegramApiException {
        messages.add(adminMsgApplyDeleteApartment.editMessage(chatId,
                ADMIN_MSG_APPLY_DELETE_APARTMENT, adminMsgStart.getSelectedApartment(chatId)));
    }
    
    private void deleteApartment(long chatId, List<Integer> messages) throws TelegramApiException {
        int selectedApartment = adminMsgStart.getSelectedApartment(chatId);

        if (service.getApartmentPath() != null)
            FileManager.deleteAllFilesFromDirectory(Paths.get(service.getApartmentPath() + selectedApartment));

        adminMsgStart.deleteApartment(selectedApartment);
        
        openListOfApartments(chatId, messages);
        
        service.sendTempMessage(chatId, ADMIN_MSG_SIMPLE_APARTMENT_DELETED, 5);
    }
    
    private void performActionStatement(long chatId,
                                        List<Integer> messages,
                                        String data,
                                        AppStatus appStatus) throws TelegramApiException {

        int numOfApp = Integer.parseInt(data);

        adminMsgStart.updateBookingCardStatus(numOfApp, appStatus);
        adminMsgStart.updateSelectedAppAdminSettings(chatId, 0);

        if (appStatus.equals(AppStatus.WAITING))
            messages.add(adminMsgArchive.editMessage(chatId, ADMIN_MSG_ARCHIVE, adminMsgStart.getCountOfArchive()));
        else
            messages.add(adminMsgNewApplications.editMessage(chatId, ADMIN_MSG_NEW_APPS, adminMsgStart.getCountOfNewApps()));

        String status;
        long userId = adminMsgStart.getUserId(numOfApp);

        switch (appStatus) {
            case WAITING -> status = service.getLocaleMessage(userId, ADMIN_MSG_STATUS_WAITING);
            case ACCEPTED -> status = service.getLocaleMessage(userId, ADMIN_MSG_STATUS_ACCEPTED);
            case DENIED -> status = service.getLocaleMessage(userId, ADMIN_MSG_STATUS_DENIED);
            case FINISHED -> status = service.getLocaleMessage(userId, ADMIN_MSG_STATUS_FINISHED);
            default -> status = "null";
        }

        service.sendMessageInfo(userId, GENERAL_MSG_UPDATED_STATUS,
                adminMsgStart.getIKMarkupOkToDelete(chatId), status);
    }

    private void openChat(long chatId, List<Integer> messages, String data) throws TelegramApiException {
        int numOfApp = Integer.parseInt(data);

        messages.add(adminMsgChat.editMessage(chatId, ADMIN_MSG_CHAT, adminMsgStart.getAppParameters(chatId, numOfApp)));
    }

    private void openApp(long chatId, List<Integer> messages, String data, App type) throws TelegramApiException {
        int numOfApp = Integer.parseInt(data);

        adminMsgStart.updateSelectedAppAdminSettings(chatId, numOfApp);

        if (type.equals(App.APP))
            messages.add(adminMsgOpenApp.editMessage(chatId, ADMIN_MSG_APP, adminMsgStart.getAppParameters(chatId, numOfApp)));
        else if (type.equals(App.ARC))
            messages.add(adminMsgOpenArc.editMessage(chatId, ADMIN_MSG_APP, adminMsgStart.getAppParameters(chatId, numOfApp)));
    }

    private void selectAmenity(long chatId, List<Integer> messages, String data) throws TelegramApiException {
        if (!adminMsgStart.isCheckingSelectedAmenities(chatId))
            adminMsgStart.insertSelectedAmenity(chatId, adminMsgStart.getAmenityById(Integer.parseInt(data)));
        else
            adminMsgStart.deleteSelectedAmenity(chatId, adminMsgStart.getAmenityById(Integer.parseInt(data)));

        if (!adminMsgStart.isEditingApartments(chatId))
            openSelectAmenities(chatId, messages);
        else
            openEditAmenities(chatId, messages, false);
    }

    private void editApartment(long chatId, List<Integer> messages, String numberOfApartment) throws TelegramApiException {
        adminMsgStart.updateSelectedApartment(chatId, Integer.parseInt(numberOfApartment));
        adminMsgStart.updateEditingApartment(chatId, true);

        int msgId = service.sendSimpleMessage(chatId, GENERAL_MSG_SIMPLE_DOWNLOADING);

        messages.addAll(adminMsgEditApartment.sendPhotos(chatId, service.getApartmentPath() + numberOfApartment));

        messages.add(adminMsgEditApartment.sendMessage(chatId, ADMIN_MSG_EDIT_APARTMENT,
                adminMsgStart.getEditApartmentParameters(chatId, numberOfApartment)));

        adminMsgStart.updateEditingPhotos(chatId, false);

        deleteMessage(chatId, msgId);
    }

    private void openEditNumber(long chatId, List<Integer> messages) throws TelegramApiException {
        messages.add(adminMsgEditNumber.editMessage(chatId, ADMIN_MSG_EDIT_NUMBER));
    }

    private void openEditPhotos(long chatId, List<Integer> messages) throws TelegramApiException {
        int msgId = service.sendSimpleMessage(chatId, GENERAL_MSG_SIMPLE_DOWNLOADING);

        if (service.getApartmentPath() != null) {
            FileManager.deleteAllFilesFromDirectory(Paths.get(service.getApartmentPath() + tempFolderOfPhotos));

            String numberOfApartment = String.valueOf(adminMsgStart.getSelectedApartment(chatId));
            messages.addAll(adminMsgEditPhotos.sendPhotos(chatId, service.getApartmentPath() + numberOfApartment));

            messages.add(adminMsgEditPhotos.sendMessage(chatId, ADMIN_MSG_EDIT_PHOTOS));

            adminMsgStart.updateEditingPhotos(chatId, true);
        } else
            service.sendMessageInfo(chatId, ADMIN_ERR_DOWNLOAD_RESOURCE, adminMsgStart.getIKMarkupOkToDelete(chatId));

        deleteMessage(chatId, msgId);
    }

    private void addPhotos(long chatId, List<Integer> messages) throws TelegramApiException {
        if (service.getApartmentPath() != null)
            FileManager.moveFiles(
                    Paths.get(service.getApartmentPath() + tempFolderOfPhotos + "/"),
                    Paths.get(service.getApartmentPath() + adminMsgStart.getSelectedApartment(chatId) + "/"),
                    ".jpg");
        else
            service.sendMessageInfo(chatId, ADMIN_ERR_DOWNLOAD_RESOURCE, adminMsgStart.getIKMarkupOkToDelete(chatId));

        editApartment(chatId, messages, String.valueOf(adminMsgStart.getSelectedApartment(chatId)));
    }

    private void replacePhotos(long chatId, List<Integer> messages) throws TelegramApiException {
        if (service.getApartmentPath() != null) {
            FileManager.deleteAllFilesFromDirectory(
                    Paths.get(service.getApartmentPath() + adminMsgStart.getSelectedApartment(chatId)));

            FileManager.moveFiles(
                    Paths.get(service.getApartmentPath() + tempFolderOfPhotos + "/"),
                    Paths.get(service.getApartmentPath() + adminMsgStart.getSelectedApartment(chatId) + "/"));
        }
        else
            service.sendMessageInfo(chatId, ADMIN_ERR_DOWNLOAD_RESOURCE, adminMsgStart.getIKMarkupOkToDelete(chatId));

        editApartment(chatId, messages, String.valueOf(adminMsgStart.getSelectedApartment(chatId)));
    }

    private void openEditArea(long chatId, List<Integer> messages) throws TelegramApiException {
        messages.add(adminMsgEditArea.editMessage(chatId, ADMIN_MSG_EDIT_AREA));
    }

    private void openEditAmenities(long chatId, List<Integer> messages, boolean isInit) throws TelegramApiException {
        if (isInit) {
            List<Amenity> amenities = adminMsgStart.getAmenitiesOfApartment(chatId);

            amenities.forEach(amenity -> adminMsgStart.insertSelectedAmenity(chatId, amenity));
        }

        messages.add(adminMsgEditAmenities.editMessage(chatId, ADMIN_MSG_EDIT_AMENITIES,
                adminMsgArchive.getStringOfSelectedAmenities(chatId)));
    }

    private void editAmenities(long chatId, List<Integer> messages, int numberOfApartment) throws TelegramApiException {
        List<Amenity> amenities = adminMsgStart.getSelectedAmenities(chatId);

        adminMsgStart.updateAmenitiesOfApartment(numberOfApartment, amenities);

        editApartment(chatId, messages, String.valueOf(adminMsgStart.getSelectedApartment(chatId)));
    }

    private void editNumber(long chatId, List<Integer> messages, String[] data) throws TelegramApiException {
        if (adminMsgStart.isEditingApartments(chatId) && data.length < 2)
            service.sendMessageInfo(chatId, ADMIN_ERR_SET_AREA, adminMsgStart.getIKMarkupOkToDelete(chatId));
        if (service.getApartmentPath() != null) {
            if (new File(service.getApartmentPath() + data[1]).exists() && adminMsgStart.isEditingApartments(chatId))
                service.sendMessageInfo(chatId, ADMIN_ERR_APARTMENT_EXISTS, adminMsgStart.getIKMarkupOkToDelete(chatId));
            else if (adminMsgStart.isEditingApartments(chatId)) {
                FileManager.moveFiles(
                        Paths.get(service.getApartmentPath() + adminMsgStart.getSelectedApartment(chatId) + "/"),
                        Paths.get(service.getApartmentPath() + data[1] + "/"));

                FileManager.deleteAllFilesFromDirectory(
                        Paths.get(service.getApartmentPath() + adminMsgStart.getSelectedApartment(chatId)));

                adminMsgStart.updateNumberApartment(adminMsgStart.getSelectedApartment(chatId), Integer.parseInt(data[1]));
                adminMsgStart.updateSelectedApartment(chatId, Integer.parseInt(data[1]));

                editApartment(chatId, messages, String.valueOf(adminMsgStart.getSelectedApartment(chatId)));
            }
        } else
            service.sendMessageInfo(chatId, ADMIN_ERR_DOWNLOAD_RESOURCE, adminMsgStart.getIKMarkupOkToDelete(chatId));
    }

    private void editArea(long chatId, List<Integer> messages, String[] data) throws TelegramApiException {
        if (adminMsgStart.isEditingApartments(chatId) && data.length < 2)
            service.sendMessageInfo(chatId, ADMIN_ERR_SET_AREA, adminMsgStart.getIKMarkupOkToDelete(chatId));
        else if (adminMsgStart.isEditingApartments(chatId)) {
            adminMsgStart.updateAreaApartment(adminMsgStart.getSelectedApartment(chatId), Double.parseDouble(data[1]));

            editApartment(chatId, messages, String.valueOf(adminMsgStart.getSelectedApartment(chatId)));
        }
    }

    private void changeLanguage(long chatId,
                                User user,
                                List<Integer> messages,
                                String data) throws TelegramApiException {
        service.setLanguage(chatId, data);
        start(chatId, user, messages, false, false);
    }

    private void deleteMessage(long chatId, int msgId) {
        service.deleteMessage(chatId, msgId);
    }
}
