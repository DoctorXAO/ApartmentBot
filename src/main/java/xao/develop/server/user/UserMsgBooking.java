package xao.develop.server.user;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.HashMap;

@Service
public class UserMsgBooking extends UserMsg
{

    HashMap<Long, String[]> userCard = new HashMap<>();

    public void createNewUserCard(Update update) {
        userCard.put(server.getChatId(update), new String[6]);
    }

    public void setName(Update update, String name) {
        String[] card = userCard.get(server.getChatId(update));
        card[0] = name;
        userCard.put(server.getChatId(update), card);
    }

    public void setSurname(Update update, String surname) {
        String[] card = userCard.get(server.getChatId(update));
        card[1] = surname;
        userCard.put(server.getChatId(update), card);
    }

    public void setGender(Update update, String gender) {
        String[] card = userCard.get(server.getChatId(update));
        card[2] = gender;
        userCard.put(server.getChatId(update), card);
    }

    public void setAge(Update update, String age) {
        String[] card = userCard.get(server.getChatId(update));
        card[3] = age;
        userCard.put(server.getChatId(update), card);
    }

    public void setCount(Update update, String count) {
        String[] card = userCard.get(server.getChatId(update));
        card[4] = count;
        userCard.put(server.getChatId(update), card);
    }

    public void setContacts(Update update, String contacts) {
        String[] card = userCard.get(server.getChatId(update));
        card[5] = contacts;
        userCard.put(server.getChatId(update), card);
    }

    @Override
    public Message sendMessage(Update update) throws TelegramApiException {
        server.deleteOldMessages(update);

        String[] card = userCard.get(server.getChatId(update));

        boolean isOneOfFieldsNull = false;

        for (String param : card)
            isOneOfFieldsNull = param == null;

        InlineKeyboardMarkup inlineKeyboardMarkup = getIKMarkup(update);

        if (!isOneOfFieldsNull)
            inlineKeyboardMarkup = getNextIKMarkup(update);

        return botConfig.getTelegramClient().execute(msgBuilder.buildSendMessage(update,
                String.format(userLoc.getLocalizationText(update),
                        card[0],
                        card[1],
                        card[2],
                        card[3],
                        card[4],
                        card[5]),
                inlineKeyboardMarkup));
    }

    public Message sendCanNotBook(Update update) throws TelegramApiException {
        server.deleteOldMessages(update);

        update.getCallbackQuery().setData(CAN_NOT_BOOK);
        return botConfig.getTelegramClient().execute(msgBuilder.buildSendMessage(update,
                userLoc.getLocalizationText(update),
                getCanNotBookIKMarkup(update)));
    }

    public InlineKeyboardMarkup getNextIKMarkup(Update update) {
        return InlineKeyboardMarkup
                .builder()
                .keyboardRow(new InlineKeyboardRow(
                        msgBuilder.buildIKButton(userLoc.getLocalizationButton(update, NEXT),
                                "new_callback")))
                .keyboardRow(new InlineKeyboardRow(
                        msgBuilder.buildIKButton(userLoc.getLocalizationButton(update, BACK),
                                RAA_QUIT_FROM_BOOKING_AN_APARTMENT)))
                .build();
    }

    @Override
    public InlineKeyboardMarkup getIKMarkup(Update update) {
        return InlineKeyboardMarkup
                .builder()
                .keyboardRow(new InlineKeyboardRow(
                        msgBuilder.buildIKButton(userLoc.getLocalizationButton(update, BACK),
                                RAA_QUIT_FROM_BOOKING_AN_APARTMENT)))
                .build();
    }

    public InlineKeyboardMarkup getCanNotBookIKMarkup(Update update) {
        return InlineKeyboardMarkup
                .builder()
                .keyboardRow(new InlineKeyboardRow(
                        msgBuilder.buildIKButton(userLoc.getLocalizationButton(update, BACK),
                                RAA_QUIT_CAN_NOT_BOOK)))
                .build();
    }
}
