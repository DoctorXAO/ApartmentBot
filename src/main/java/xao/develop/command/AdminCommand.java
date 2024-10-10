package xao.develop.command;

public interface AdminCommand {
    String START = "/start";
    String CHAT = "/chat";
    String NUMBER = "/number";
    String AREA = "/area";

    String NEW_APPLICATIONS = "new_applications";
    String ARCHIVE = "archive";
    String SETTINGS = "settings";

    String NEW_APARTMENT = "new_apartment";
    String BACK_TO_EDIT_NEW_APARTMENT = "back_to_edit_new_apartment";
    String LIST_OF_APARTMENTS = "list_of_apartments";

    String QUIT_FROM_APP = "quit_from_app";
    String QUIT_FROM_ARC = "quit_from_arc";
    String QUIT_FROM_NEW_APARTMENT = "quit_from_new_apartment";
    String QUIT_FROM_LIST_OF_APARTMENTS = "quit_from_list_of_apartments";

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
}