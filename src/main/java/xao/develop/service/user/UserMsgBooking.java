package xao.develop.service.user;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import xao.develop.model.TempBookingData;

@Service
public class UserMsgBooking extends UserMessage
{

    public void setName(long chatId, String name) {
        persistence.updateFirstNameTempBookingData(chatId, name);
    }

    public void setSurname(long chatId, String surname) {
        persistence.updateLastNameTempBookingData(chatId, surname);
    }

    public void setGender(long chatId, String gender) {
        persistence.updateGenderTempBookingData(chatId, gender);
    }

    public void setAge(long chatId, String age) {
        persistence.updateAgeTempBookingData(chatId, age);
    }

    public void setCount(long chatId, String count) {
        persistence.updateCountOfPeopleTempBookingData(chatId, count);
    }

    public void setContacts(long chatId, String contacts) {
        persistence.updateContactsTempBookingData(chatId, contacts);
    }

    public Object[] getTempBookingData(long chatId) {
        TempBookingData tempBookingData = persistence.selectTempBookingData(chatId);

        return new Object[]{
                tempBookingData.getFirstName(),
                tempBookingData.getLastName(),
                tempBookingData.getGender(),
                tempBookingData.getAge(),
                tempBookingData.getCountOfPeople(),
                tempBookingData.getContacts()
        };
    }

    @Override
    public InlineKeyboardMarkup getIKMarkup(Update update) {
        boolean isOneOfFieldsNull = false;

        Object[] parameters = getTempBookingData(service.getChatId(update));

        for (Object param : parameters)
            if (param == null || param.equals("0")) {
                isOneOfFieldsNull = true;
                break;
            }

        if (isOneOfFieldsNull)
            return getBackIKMarkup(update);
        else
            return getNextIKMarkup(update);
    }

    private InlineKeyboardMarkup getBackIKMarkup(Update update) {
        return InlineKeyboardMarkup
                .builder()
                .keyboardRow(new InlineKeyboardRow(
                        msgBuilder.buildIKButton(service.getLocaleMessage(service.getChatId(update), GENERAL_BT_BACK),
                                RAA_QUIT_FROM_BOOKING_AN_APARTMENT)))
                .build();
    }

    private InlineKeyboardMarkup getNextIKMarkup(Update update) {
        return InlineKeyboardMarkup
                .builder()
                .keyboardRow(new InlineKeyboardRow(
                        msgBuilder.buildIKButton(service.getLocaleMessage(service.getChatId(update), USER_BT_NEXT),
                                RAA_SHOW_PREVIEW)))
                .keyboardRow(new InlineKeyboardRow(
                        msgBuilder.buildIKButton(service.getLocaleMessage(service.getChatId(update), GENERAL_BT_BACK),
                                RAA_QUIT_FROM_BOOKING_AN_APARTMENT)))
                .build();
    }
}
