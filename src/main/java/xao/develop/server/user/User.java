package xao.develop.server.user;

import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.MaybeInaccessibleMessage;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.List;

public interface User {

    void registerMessage(Long chatId, int messageId);
    void deleteOldMessages(Update update);

    SendMediaGroup sendPhotos(Update update, String patch);

    void authorization(Message message);

    boolean isUserStatusExists(Long chatId);

    void changeUserLanguage(MaybeInaccessibleMessage message, String language);
    void setUserFillingOutStep(Long chatId, Integer fillingOutStep);
    Integer getUserFillingOutStep(Long chatId);
    void setUserApplicationName(Message message);
    void setUserApplicationCountOfPerson(Message message);
    void setUserApplicationTimeRent(Message message);
    void setUserApplicationCommentary(Message message);

    String getLocalizationText(Update update);
    String getLocalizationButton(Update update, String nameButton);

    InlineKeyboardMarkup getMainIKMarkup(Update update);
    InlineKeyboardMarkup getHouseInformationIKMarkup(Update update);
    InlineKeyboardMarkup getRentAnApartmentIKMarkup(Update update);
    InlineKeyboardMarkup getChangeLanguageIKMarkup(Update update);
    InlineKeyboardMarkup getBackIKMarkup(Update update, String direction);

    InlineKeyboardRow buildIKRow(List<InlineKeyboardButton> buttons);
    InlineKeyboardButton buildIKButton(String text, String callbackData);
    SendMessage buildSendMessage(Update update, String text, InlineKeyboardMarkup markup);
}
