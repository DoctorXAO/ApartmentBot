package xao.develop.config;

public interface AdminCommand {
    String START = "/start";

    String NEW_APPLICATIONS = "new_applications";
    String ARCHIVE = "archive";
    String SETTINGS = "settings";

    String QUIT_FROM_APP = "quit_from_app";
    String QUIT_FROM_ARC = "quit_from_arc";
    String BACK_TO_START = "back_to_start";

    String PREVIOUS_PAGE_OF_ARCHIVE = "previous_page_of_archive";
    String NEXT_PAGE_OF_ARCHIVE = "next_page_of_archive";
    String PREVIOUS_PAGE_OF_NEW_APPS = "previous_page_of_new_apps";
    String NEXT_PAGE_OF_NEW_APPS = "next_page_of_new_apps";

    String ARC = "arc";
    String ACCEPT_APP = "accept_app";
    String REFUSE_APP = "refuse_app";
    String RETURN_APP = "return_arc";

    String OPEN_CHAT = "open_chat";

    String EMPTY = "empty";
}
