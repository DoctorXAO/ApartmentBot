package xao.develop.server.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import xao.develop.config.UserCommand;
import xao.develop.model.UserStatus;
import xao.develop.repository.UserPersistence;

import java.util.Locale;

@Slf4j
@Service
public class UserLocalization implements UserCommand {
    @Autowired
    UserPersistence userPersistence;

    @Autowired
    MessageSource messageSource;

    private String getLocaleMessage(Update update, String code) {
        UserStatus userStatus = update.hasMessage() ?
                userPersistence.selectUserStatus(update.getMessage().getChatId()) :
                userPersistence.selectUserStatus(update.getCallbackQuery().getMessage().getChatId());

        Locale locale = new Locale(userStatus.getLanguage());

        return messageSource.getMessage(code, null, locale);
    }

    public String getLocalizationText(Update update) {
        log.trace("Method getLocalizationText(Update) started");

        String signal = update.hasMessage() ?
                update.getMessage().getText() :
                update.getCallbackQuery().getData();

        try {
            String text;

            switch (signal) {
                case START -> text = getLocaleMessage(update, "user.msg.start");
                default -> throw new Exception("Error download message");
            }

            log.debug("Method getLocalizationText(Update) is returning the next value: {}", signal);
            log.trace("Method getLocalizationText(Update) finished");

            return text;
        } catch (Exception ex) {
            log.error("Error loading message. Signal: {}. Error: {}", signal, ex.getMessage());

            return "Error download message. Please, to message the system administrator.";
        }
    }

    public String getLocalizationButton(Update update, String nameButton) {
        log.trace("Method getLocalizationButton(Update, String) started");
        log.debug("getLocalizationButton: nameButton = {}", nameButton);

        String text;

        try {
            switch (nameButton) {
                case APARTMENTS -> text = getLocaleMessage(update, "user.bt.apartments");
                case RENT_AN_APARTMENT -> text = getLocaleMessage(update, "user.bt.rent-an-apartment");
                case HOUSE_INFORMATION -> text = getLocaleMessage(update, "user.bt.house-information");
                case CONTACTS -> text = getLocaleMessage(update, "user.bt.contacts");
                case CHANGE_LANGUAGE -> text = getLocaleMessage(update, "user.bt.change-language");
                default -> throw new Exception("Ошибка загрузки названия кнопки");
            }
        } catch (Exception ex) {
            log.error("Unknown name button. Name button: {}", nameButton);

            return "null";
        }

        log.trace("Method getLocalizationButton(Update, String) finished");

        return text;
    }
}
