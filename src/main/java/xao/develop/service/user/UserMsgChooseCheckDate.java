package xao.develop.service.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import xao.develop.model.BookingCard;
import xao.develop.config.enums.TypeOfAppStatus;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Slf4j
@Service
public class UserMsgChooseCheckDate extends UserDate {

    public void deleteUserFromTempBookingData(Update update) {
        persistence.deleteTempBookingData(service.getChatId(update));

        log.debug("The next user from UserCalendar deleted: {}", service.getChatId(update));
    }

    public boolean checkIsAlreadyExistRent(long chatId) {
        List<BookingCard> bookingCards = persistence.selectBookingCardByStatus(TypeOfAppStatus.WAITING);
        bookingCards.addAll(persistence.selectBookingCardByStatus(TypeOfAppStatus.ACCEPTED));

        for (BookingCard bookingCard : bookingCards)
            if (bookingCard.getChatId() == chatId)
                return true;

        return false;
    }

    @Override
    public InlineKeyboardMarkup getIKMarkup(long chatId) {
        List<InlineKeyboardRow> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> buttons = new ArrayList<>();

        Calendar presentTime = getPresentTime(chatId);
        Calendar selectedTime = getSelectedTime(chatId);

        int maxPresentDaysOfMonth = getMaxDaysOfMonth(selectedTime, 0);
        int firstDayOfWeekInMonth = getFirstDayOfWeekInMonth(selectedTime);
        int presentDayOfMonth = getPresentDayOfMonth(presentTime);

        initHeaderIKMarkup(chatId, keyboard, buttons, presentTime, selectedTime);

        // Добавляет недостающие дни недели из последних дней предыдущего месяца
        if (firstDayOfWeekInMonth != 1)
            for (int i = firstDayOfWeekInMonth - 2; i >= 0; i--)
                buttons.add(msgBuilder.buildIKButton("🛑", EMPTY));

        boolean areDatesEquals = checkEqualsDate(chatId);

        log.debug("areDatesEquals = {}", areDatesEquals);

        // Добавляет все дни выбранного месяца по неделям
        for (int i = 1; i <= maxPresentDaysOfMonth; i++) {

            if (selectedTime.get(Calendar.YEAR) >= presentTime.get(Calendar.YEAR) + MAX_YEAR &&
                    selectedTime.get(Calendar.MONTH) >= presentTime.get(Calendar.MONTH) &&
                    i > presentTime.get(Calendar.DAY_OF_MONTH))
                buttons.add(msgBuilder.buildIKButton("🛑", EMPTY));
            else if (i >= presentDayOfMonth || !areDatesEquals)
                buttons.add(msgBuilder.buildIKButton(String.valueOf(i), RAA_SET_DAY + i));
            else
                buttons.add(msgBuilder.buildIKButton("🛑", EMPTY));

            // Добавляет все недостающие дни недели из первых дней следующего месяца
            if (i == maxPresentDaysOfMonth && getDayOfWeekInMonth(selectedTime, i) != 7) {
                int difference = 7 - getDayOfWeekInMonth(selectedTime, i);
                for (int j = 1; j <= difference; j++)
                    buttons.add(msgBuilder.buildIKButton("🛑", EMPTY));
                keyboard.add(msgBuilder.buildIKRow(buttons));
                buttons.clear();
                break;
            }

            // Если последний день недели, то оборвать линию и начать новую
            if (getDayOfWeekInMonth(selectedTime, i) == 7) {
                keyboard.add(msgBuilder.buildIKRow(buttons));
                buttons.clear();
            }
        }

        buttons.add(msgBuilder.buildIKButton(
                service.getLocaleMessage(chatId, GENERAL_BT_BACK), RAA_QUIT_FROM_CHOOSER_CHECK));
        keyboard.add(msgBuilder.buildIKRow(buttons));

        return InlineKeyboardMarkup
                .builder()
                .keyboard(keyboard)
                .build();
    }

    private void initHeaderIKMarkup(long chatId,
                                    List<InlineKeyboardRow> keyboard,
                                    List<InlineKeyboardButton> buttons,
                                    Calendar today,
                                    Calendar calendar) {

        if (today.get(Calendar.YEAR) < calendar.get(Calendar.YEAR))
            buttons.add(msgBuilder.buildIKButton("◀️", RAA_PREVIOUS_CHECK_YEAR));
        else
            buttons.add(msgBuilder.buildIKButton("🛑", EMPTY));

        buttons.add(msgBuilder.buildIKButton(getSelectedYear(calendar), RAA_CHANGE_CHECK_YEAR));

        if (calendar.get(Calendar.YEAR) < today.get(Calendar.YEAR) + MAX_YEAR)
            buttons.add(msgBuilder.buildIKButton("▶️", RAA_NEXT_CHECK_YEAR));
        else
            buttons.add(msgBuilder.buildIKButton("🛑", EMPTY));

        keyboard.add(msgBuilder.buildIKRow(buttons));
        buttons.clear();

        if (today.get(Calendar.MONTH) < calendar.get(Calendar.MONTH) || today.get(Calendar.YEAR) < calendar.get(Calendar.YEAR))
            buttons.add(msgBuilder.buildIKButton("◀️", RAA_PREVIOUS_CHECK_MONTH));
        else
            buttons.add(msgBuilder.buildIKButton("🛑", EMPTY));

        buttons.add(msgBuilder.buildIKButton(
                service.getLocaleMessage(chatId, USER_BT_MONTH_ + (calendar.get(Calendar.MONTH) + 1)),
                RAA_CHANGE_CHECK_MONTH));

        if (calendar.get(Calendar.MONTH) < today.get(Calendar.MONTH) ||
                calendar.get(Calendar.YEAR) < today.get(Calendar.YEAR) + MAX_YEAR)
            buttons.add(msgBuilder.buildIKButton("▶️", RAA_NEXT_CHECK_MONTH));
        else
            buttons.add(msgBuilder.buildIKButton("🛑", EMPTY));

        keyboard.add(msgBuilder.buildIKRow(buttons));
        buttons.clear();
    }
}
