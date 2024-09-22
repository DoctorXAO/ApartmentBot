package xao.develop.server.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import xao.develop.config.UserCommand;
import xao.develop.model.AccountStatus;
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
        AccountStatus accountStatus = update.hasMessage() ?
                persistence.selectAccountStatus(update.getMessage().getChatId()) :
                persistence.selectAccountStatus(update.getCallbackQuery().getMessage().getChatId());

        Locale locale = new Locale(accountStatus.getLanguage());

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
                case RAA_CHANGE_CHECK_IN_MONTH -> text = getLocaleMessage(update, "user.msg.change-check-in-month");
                case RAA_CHANGE_CHECK_IN_YEAR -> text = getLocaleMessage(update, "user.msg.change-check-in-year");
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
                case "month_1" -> text = getLocaleMessage(update, "user.bt.january");
                case "month_2" -> text = getLocaleMessage(update, "user.bt.february");
                case "month_3" -> text = getLocaleMessage(update, "user.bt.march");
                case "month_4" -> text = getLocaleMessage(update, "user.bt.april");
                case "month_5" -> text = getLocaleMessage(update, "user.bt.may");
                case "month_6" -> text = getLocaleMessage(update, "user.bt.june");
                case "month_7" -> text = getLocaleMessage(update, "user.bt.july");
                case "month_8" -> text = getLocaleMessage(update, "user.bt.august");
                case "month_9" -> text = getLocaleMessage(update, "user.bt.september");
                case "month_10" -> text = getLocaleMessage(update, "user.bt.october");
                case "month_11" -> text = getLocaleMessage(update, "user.bt.november");
                case "month_12" -> text = getLocaleMessage(update, "user.bt.december");
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
