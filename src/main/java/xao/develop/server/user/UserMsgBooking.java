package xao.develop.server.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import xao.develop.model.TempBookingData;
import xao.develop.repository.Persistence;

@Service
public class UserMsgBooking extends UserMsg
{
    @Autowired
    Persistence persistence;

    public void setName(Update update, String name) {
        persistence.updateFirstNameTempBookingData(server.getChatId(update), name);
    }

    public void setSurname(Update update, String surname) {
        persistence.updateLastNameTempBookingData(server.getChatId(update), surname);
    }

    public void setGender(Update update, String gender) {
        persistence.updateGenderTempBookingData(server.getChatId(update), gender);
    }

    public void setAge(Update update, String age) {
        persistence.updateAgeTempBookingData(server.getChatId(update), age);
    }

    public void setCount(Update update, String count) {
        persistence.updateCountOfPeopleTempBookingData(server.getChatId(update), count);
    }

    public void setContacts(Update update, String contacts) {
        persistence.updateContactsTempBookingData(server.getChatId(update), contacts);
    }

    public TempBookingData getTempBookingData(Update update) {
        return persistence.selectTempBookingData(server.getChatId(update));
    }

    @Override
    public InlineKeyboardMarkup getIKMarkup(Update update) {
        TempBookingData tempBookingData = getTempBookingData(update);

        boolean isOneOfFieldsNull = false;

        String[] parameters = new String[6];
        parameters[0] = tempBookingData.getFirstName();
        parameters[1] = tempBookingData.getLastName();
        parameters[2] = tempBookingData.getGender();
        parameters[3] = String.valueOf(tempBookingData.getAge());
        parameters[4] = String.valueOf(tempBookingData.getCountOfPeople());
        parameters[5] = tempBookingData.getContacts();

        for (String param : parameters)
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
                        msgBuilder.buildIKButton(server.getLocaleMessage(update, USER_BT_BACK),
                                RAA_QUIT_FROM_BOOKING_AN_APARTMENT)))
                .build();
    }

    private InlineKeyboardMarkup getNextIKMarkup(Update update) {
        return InlineKeyboardMarkup
                .builder()
                .keyboardRow(new InlineKeyboardRow(
                        msgBuilder.buildIKButton(server.getLocaleMessage(update, USER_BT_NEXT),
                                RAA_SHOW_PREVIEW)))
                .keyboardRow(new InlineKeyboardRow(
                        msgBuilder.buildIKButton(server.getLocaleMessage(update, USER_BT_BACK),
                                RAA_QUIT_FROM_BOOKING_AN_APARTMENT)))
                .build();
    }
}
