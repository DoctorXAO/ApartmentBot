package xao.develop.command;

public interface AdminCommand {
    String START = "/start";
    String CHAT = "/chat";

    String NUMBER = "/number";
    String PHOTOS = "/photos";
    String AREA = "/area";
    String AMENITIES = "/amenities";

    String SET_NUMBER = "/set_number";
    String SET_AREA = "/set_area";

    String NEW_APPLICATIONS = "new_applications";
    String ARCHIVE = "archive";
    String SETTINGS = "settings";

    String OPEN_NEW_APARTMENT = "new_apartment";
    String BACK_TO_EDIT_NEW_APARTMENT = "back_to_edit_new_apartment";
    String LIST_OF_APARTMENTS = "list_of_apartments";
    String BACK_TO_EDIT_NEW_AMENITY = "back_to_edit_new_amenity";
    String LIST_OF_AMENITIES = "list_of_amenities";

    String QUIT_FROM_APP = "quit_from_app";
    String QUIT_FROM_ARC = "quit_from_arc";
    String QUIT_TO_SETTINGS = "quit_to_settings";

    String PREVIOUS_PAGE_OF_ARCHIVE = "previous_page_of_archive";
    String NEXT_PAGE_OF_ARCHIVE = "next_page_of_archive";
    String PREVIOUS_PAGE_OF_NEW_APPS = "previous_page_of_new_apps";
    String NEXT_PAGE_OF_NEW_APPS = "next_page_of_new_apps";
    String PREVIOUS_PAGE_OF_APART = "previous_page_of_apartments";
    String NEXT_PAGE_OF_APART = "next_page_of_apartments";
    String PREVIOUS_PAGE_OF_AMENITIES = "previous_page_of_amenities";
    String NEXT_PAGE_OF_AMENITIES = "next_page_of_amenities";

    String PREVIEW_APARTMENT = "preview_apartment";
    String CREATE_APARTMENT = "create_apartment";
    String SELECTED = "selected";
    String AVAILABLE = "available";

    String ARC = "arc";
    String ACCEPT_APP = "accept_app";
    String REFUSE_APP = "refuse_app";
    String RETURN_APP = "return_arc";

    String OPEN_CHAT = "open_chat";

    String EMPTY = "empty";

    String APARTMENT = "apartment";
    String AMENITY = "amenity";

    String OPEN_EDIT_NUMBER = "open_edit_number";
    String OPEN_EDIT_PHOTOS = "open_edit_photos";
    String ADD_PHOTOS = "add_photos";
    String REPLACE_PHOTOS = "replace_photos";
    String OPEN_EDIT_AREA = "open_edit_area";
    String OPEN_EDIT_AMENITIES = "open_edit_amenities";
    String EDIT_AMENITIES = "edit_amenities";
    String APPLY_DELETE_APARTMENT = "apply_delete_apartment";
    String DELETE_APARTMENT = "delete_apartment";

    // Create amenity

    String SET_LINK = "/set_link";
    String SET_NAME_EN = "/set_name_en";
    String SET_NAME_TR = "/set_name_tr";
    String SET_NAME_RU = "/set_name_ru";
    String SET_IMPORTANCE = "/set_importance";

    String OPEN_NEW_AMENITY = "open_new_amenity";
    String CREATE_NEW_AMENITY = "create_new_amenity";
    String QUIT_FROM_NEW_AMENITY = "quit_from_new_amenity";

    // Edit amenity

    String ED_AMENITY = "ed_amenity";
    String APPLY_DELETE_AMENITY = "apply_delete_amenity";

    String DELETE_AMENITY = "delete_amenity";
}
