package xao.develop.server.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import xao.develop.model.Apartments;
import xao.develop.repository.Persistence;
import xao.develop.server.Server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Service
public class UserMsgChooseAnApartment extends UserMsg {

    @Autowired
    Server server;

    @Autowired
    Persistence persistence;

    HashMap<Long, Integer> userSelector = new HashMap<>();

    public Long getCurrentApartment(Update update) {
        int currentSelector = userSelector.get(server.getChatId(update));
        List<Apartments> apartments = persistence.selectAllApartments();

        log.debug("Size of the list of apartment is {}", apartments.size());
        log.debug("Current selector is {}", currentSelector);

        return apartments.get(currentSelector).getNumber();
    }

    public void addUserToSelector(Update update) {
        userSelector.put(server.getChatId(update), 0);

        log.debug("""
                Method addUserToSelector(Update): added the next user with parameters:
                chatId = {}
                current selector = {}""",
                server.getChatId(update), userSelector.get(server.getChatId(update)));
    }

    public void upSelector(Update update) {
        if (userSelector.get(server.getChatId(update)) + 1 < persistence.selectAllApartments().size()) {
            userSelector.put(server.getChatId(update), userSelector.get(server.getChatId(update)) + 1);

            log.debug("""
                    Method upSelector(Update): user selector upped:
                    chatId = {}
                    current selector = {}""",
                    server.getChatId(update), userSelector.get(server.getChatId(update)));
        } else
            log.debug("Method upSelector(Update): can't up selector because {} is max!", userSelector.get(server.getChatId(update)));
    }

    public void downSelector(Update update) {
        if (userSelector.get(server.getChatId(update)) - 1 >= 0) {
            userSelector.put(server.getChatId(update), userSelector.get(server.getChatId(update)) - 1);

            log.debug("""
                    Method downSelector(Update): user selector downed:
                    chatId = {}
                    current selector = {}""",
                    server.getChatId(update), userSelector.get(server.getChatId(update)));
        } else
            log.debug("Method downSelector(Update): can't down selector because {} is min!", userSelector.get(server.getChatId(update)));
    }

    public void deleteUserFromSelector(Update update) {
        userSelector.remove(server.getChatId(update));

        log.debug("Method deleteUserFromSelector(Update): the next user deleted: {} ", server.getChatId(update));
    }

    @Override
    public Message sendMessage(Update update) throws TelegramApiException {
        server.deleteOldMessages(update);

        List<Apartments> apartments = persistence.selectAllApartments();
        Apartments apartment = apartments.get(userSelector.get(server.getChatId(update)));

        return botConfig.getTelegramClient().execute(msgBuilder.buildSendMessage(update,
                String.format(userLoc.getLocalizationText(update),
                        apartment.getStatus(), apartment.getArea(), apartment.getAmenities()),
                getIKMarkup(update)));
    }

    @Override
    public InlineKeyboardMarkup getIKMarkup(Update update) {
        List<InlineKeyboardButton> buttons = new ArrayList<>();

        if (userSelector.get(server.getChatId(update)) > 0)
            buttons.add(msgBuilder.buildIKButton("◀️", BACK_APARTMENT));
        else
            buttons.add(msgBuilder.buildIKButton("\uD83D\uDED1", END));

        if (userSelector.get(server.getChatId(update)) < persistence.selectAllApartments().size() - 1)
            buttons.add(msgBuilder.buildIKButton("▶️", NEXT_APARTMENT));
        else
            buttons.add(msgBuilder.buildIKButton("\uD83D\uDED1", END));

        InlineKeyboardRow row1 = msgBuilder.buildIKRow(buttons);

        buttons.clear();
        buttons.add(msgBuilder.buildIKButton(userLoc.getLocalizationButton(update, BOOK), BOOK));
        InlineKeyboardRow row2 = msgBuilder.buildIKRow(buttons);

        buttons.clear();
        buttons.add(msgBuilder.buildIKButton(userLoc.getLocalizationButton(update, BACK), QUIT_FROM_CHOOSER));
        InlineKeyboardRow row3 = msgBuilder.buildIKRow(buttons);

        return InlineKeyboardMarkup
                .builder()
                .keyboardRow(row1)
                .keyboardRow(row2)
                .keyboardRow(row3)
                .build();
    }
}
