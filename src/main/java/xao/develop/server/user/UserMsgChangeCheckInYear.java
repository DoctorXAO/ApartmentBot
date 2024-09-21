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
import java.util.List;

@Service
public class UserMsgChangeCheckInYear extends UserMsg {

    @Autowired
    Persistence persistence;

    @Autowired
    Server server;

    @Autowired
    DateService dateS;

    @Override
    public InlineKeyboardMarkup getIKMarkup(Update update) {
        Calendar presentTime = persistence.getServerPresentTime();
        Calendar selectedTime = Calendar.getInstance();
        selectedTime.setTimeInMillis(persistence.selectTempBookingData(server.getChatId(update)).getSelectedTime());

        List<InlineKeyboardRow> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> buttons = new ArrayList<>();

        buttons.add(msgBuilder.buildIKButton(userLoc.getLocalizationButton(update, BACK),
                RAA_QUIT_FROM_CHANGE_CHECK_IN_MONTH));
        keyboard.add(msgBuilder.buildIKRow(buttons));
        buttons.clear();

        for (int i = 1; i <= 12; i++) {
            selectedTime.set(Calendar.MONTH, i - 1); // -1 - так как класс Calendar ведет счет месяца с 0

            if (presentTime.get(Calendar.YEAR) < selectedTime.get(Calendar.YEAR))
                buttons.add(msgBuilder.buildIKButton(userLoc.getLocalizationButton(update, "month_" + i),
                        RAA_SET_MONTH + i));
            else if (presentTime.get(Calendar.MONTH) <= selectedTime.get(Calendar.MONTH))
                buttons.add(msgBuilder.buildIKButton(userLoc.getLocalizationButton(update, "month_" + i),
                        RAA_SET_MONTH + i));
            else
                buttons.add(msgBuilder.buildIKButton("🛑", EMPTY));

            keyboard.add(msgBuilder.buildIKRow(buttons));
            buttons.clear();
        }

        return InlineKeyboardMarkup
                .builder()
                .keyboard(keyboard)
                .build();
    }
}
