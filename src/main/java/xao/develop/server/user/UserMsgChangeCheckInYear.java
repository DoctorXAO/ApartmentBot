package xao.develop.server.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import xao.develop.repository.Persistence;
import xao.develop.server.Server;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

@Service
public class UserMsgChangeCheckInYear extends UserMsg {

    private final int MAX_YEAR = 10;

    @Autowired
    Persistence persistence;

    @Autowired
    Server server;

    @Autowired
    DateService dateS;

    public void setYear(Update update, int year) {
        Calendar selectedTime = dateS.getSelectedTime(update);

        selectedTime.set(Calendar.YEAR, year);

        persistence.updateTempBookingData(server.getChatId(update),
                Math.max(persistence.getServerPresentTime().getTimeInMillis(), selectedTime.getTimeInMillis()));
    }

    @Override
    public InlineKeyboardMarkup getIKMarkup(Update update) {
        Calendar presentTime = persistence.getServerPresentTime();

        List<InlineKeyboardRow> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> buttons = new ArrayList<>();

        buttons.add(msgBuilder.buildIKButton(userLoc.getLocalizationButton(update, BACK),
                RAA_QUIT_FROM_CHANGE_CHECK_IN_MONTH));
        keyboard.add(msgBuilder.buildIKRow(buttons));
        buttons.clear();

        for (int i = 0; i <= MAX_YEAR; i++) {
            String year = String.valueOf(presentTime.get(Calendar.YEAR) + i);
            buttons.add(msgBuilder.buildIKButton(year, RAA_SET_YEAR + year));

            keyboard.add(msgBuilder.buildIKRow(buttons));
            buttons.clear();
        }

        return InlineKeyboardMarkup
                .builder()
                .keyboard(keyboard)
                .build();
    }
}
