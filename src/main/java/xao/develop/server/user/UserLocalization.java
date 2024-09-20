package xao.develop.server.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import xao.develop.config.UserCommand;
import xao.develop.model.UserStatus;
import xao.develop.repository.Persistence;

import java.util.Locale;

@Slf4j
@Service
public class UserLocalization implements UserCommand {
    @Autowired
    Persistence persistence;

    @Autowired
    MessageSource messageSource;

    private String getLocaleMessage(Update update, String code) {
        UserStatus userStatus = update.hasMessage() ?
                persistence.selectUserStatus(update.getMessage().getChatId()) :
                persistence.selectUserStatus(update.getCallbackQuery().getMessage().getChatId());

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
                case APARTMENTS -> text = getLocaleMessage(update, "user.msg.apartments");
                case RAA_RENT_AN_APARTMENT -> text = getLocaleMessage(update, "user.msg.rent-an-apartment");
                case HI_HOUSE_INFORMATION -> text = getLocaleMessage(update, "user.msg.house-information");
                case CONTACTS -> text = getLocaleMessage(update, "user.msg.contacts");
                case CHANGE_LANGUAGE -> text = getLocaleMessage(update, "user.msg.change-language");
                case HI_RULES -> text = getLocaleMessage(update, "user.msg.rules");
                case RAA_CHOOSE_CHECK_IN_DATE -> text = getLocaleMessage(update, "user.msg.choose-check-in-date");
                case RAA_CHOOSE_AN_APARTMENT -> text = getLocaleMessage(update, "user.msg.choose-an-apartment");
                case RAA_BOOK -> text = getLocaleMessage(update, "user.msg.book");
                default -> throw new Exception("Error download message");
            }

            log.debug("Method getLocalizationText(Update) is returning the next value: {}", signal);
            log.trace("Method getLocalizationText(Update) finished");

            return text;
        } catch (Exception ex) {
            log.warn("Error loading message. Signal: {}. Error: {}", signal, ex.getMessage());

            return "Error loading message. Signal: " + signal + " Please send a bug report to us.";
        }
    }

    public String getLocalizationButton(Update update, String nameButton) {
        log.trace("Method getLocalizationButton(Update, String) started");
        log.debug("getLocalizationButton: nameButton = {}", nameButton);

        String text;

        try {
            switch (nameButton) {
                case APARTMENTS -> text = getLocaleMessage(update, "user.bt.apartments");
                case RAA_RENT_AN_APARTMENT -> text = getLocaleMessage(update, "user.bt.rent-an-apartment");
                case HI_HOUSE_INFORMATION -> text = getLocaleMessage(update, "user.bt.house-information");
                case CONTACTS -> text = getLocaleMessage(update, "user.bt.contacts");
                case CHANGE_LANGUAGE -> text = getLocaleMessage(update, "user.bt.change-language");
                case HI_RULES -> text = getLocaleMessage(update, "user.bt.rules");
                case RAA_CHOOSE_CHECK_IN_DATE -> text = getLocaleMessage(update, "user.bt.choose-check-in-date");
                case RAA_CHOOSE_AN_APARTMENT -> text = getLocaleMessage(update, "user.bt.choose-an-apartment");
                case RAA_BOOK -> text = getLocaleMessage(update, "user.bt.book");
                case BACK -> text = getLocaleMessage(update, "user.bt.back");
                default -> throw new Exception("Error loading button name");
            }
        } catch (Exception ex) {
            log.warn("Unknown name button. Name button: {}", nameButton);

            return "null";
        }

        log.trace("Method getLocalizationButton(Update, String) finished");

        return text;
    }
}
