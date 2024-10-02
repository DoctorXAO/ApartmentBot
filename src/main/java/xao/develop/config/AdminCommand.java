package xao.develop.config;

public interface AdminCommand {
    String START = "/start";

    String NEW_APPLICATIONS = "new_applications";
    String ARCHIVE = "archive";
    String SETTINGS = "settings";
    String CHANGE_LANGUAGE = "change_language";

    String BACK_TO_START = "back_to_start";

    String PARAM = "param_";
    String APP = PARAM + "app_";
    String ACCEPT_APP = PARAM + "accept_app_";
    String REFUSE_APP = PARAM + "refuse_app_";
}
