package xao.develop.view.User;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import xao.develop.config.BotConfig;
import xao.develop.config.UserCommand;
import xao.develop.service.UserParameter;
import xao.develop.service.UserService;
import xao.develop.view.Account;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class UserView implements Account, User, UserCommand {

    @Autowired
    private BotConfig botConfig;

    @Autowired
    private UserService userService;

    @Autowired
    private UserParameter userParameter;

    @Override
    public void core(Update update) {
        if ((update.hasMessage() && update.getMessage().hasText()) || update.hasCallbackQuery())
            userService.deleteOldMessages(update);

        int fillingOutStep = 0;

        if (userService.isUserStatusExists(userParameter.getChatId(update)))
            fillingOutStep = userService.getUserFillingOutStep(userParameter.getChatId(update));

        System.out.println("Step: " + fillingOutStep);

        try {
            if (update.hasMessage() && fillingOutStep >= 1) {
                System.out.println("Direction: processingTheApplication");
                processingTheApplication(update);
            }
            else if (update.hasCallbackQuery() && fillingOutStep >= 1) {
                System.out.println("Direction: processingTheApplicationCallbackQuery");
                processingTheApplicationCallbackQuery(update);
            }
            else if (update.hasMessage() && fillingOutStep < 1) {
                System.out.println("Direction: processingTheMessage");
                processingTheMessage(update);
            }
            else if (update.hasCallbackQuery()) {
                System.out.println("Direction: processingTheCallbackQuery");
                processingTheCallbackQuery(update);
            }
        } catch (TelegramApiException ex) {
            ex.printStackTrace(); // todo Замени на логи
        }
    }

    /** Обработка заполнения заявления **/
    private void processingTheApplication(Update update) throws TelegramApiException {
        deleteLastMessages(update);

        int step = userService.getUserFillingOutStep(userParameter.getChatId(update));
        userService.setUserFillingOutStep(userParameter.getChatId(update), step + 1);

        userService.registerMessage(
                userParameter.getChatId(update),
                app_nextStep(update, step).getMessageId());
    }

    private void processingTheApplicationCallbackQuery(Update update) throws TelegramApiException {
        String data = update.getCallbackQuery().getData();

        System.out.println("Data: " + data);

        List<Message> messages = new ArrayList<>();

        int step = userService.getUserFillingOutStep(userParameter.getChatId(update));
        step--;
        userService.setUserFillingOutStep(userParameter.getChatId(update), step);

        System.out.println("pro Step: " + step);

        if (step == 0 && data.equals(BACK)) {
            System.out.println("Check in");

            update.getCallbackQuery().setData(RENT_AN_APARTMENT);

            Integer msgId = botConfig.getTelegramClient().execute(userService.buildSendMessage(update,
                    userService.getLocalizationText(update),
                    userService.getRentAnApartmentIKMarkup(update))).getMessageId();

            userService.registerMessage(userParameter.getChatId(update), msgId);
        }
        else if (step != 0)
            switch (data) {
                case BACK -> {
                    Integer msgId = botConfig.getTelegramClient().execute(userService.buildSendMessage(update,
                            userService.getLocalizationText(update),
                            userService.getBackIKMarkup(update, BACK))).getMessageId();

                    userService.registerMessage(userParameter.getChatId(update), msgId);
                }
                case RENT_AN_APARTMENT -> messages = data_rent_an_apartment(update);

                default -> {
                    userService.setUserFillingOutStep(userParameter.getChatId(update), 0);

                    System.out.println("ВНИМАНИЕ: Не распознана команда в методе processingTheApplicationCallbackQuery");

                    messages = data_start(update);
                }
            }
        else {
            Integer msgId = botConfig.getTelegramClient().execute(userService.buildSendMessage(update,
                    userService.getLocalizationText(update),
                    userService.getRentAnApartmentIKMarkup(update))).getMessageId();

            userService.registerMessage(userParameter.getChatId(update), msgId);
        }

        for (Message message : messages) {
            if (message == null)
                continue;

            userService.registerMessage(message.getChatId(), message.getMessageId());
        }
    }

    /** Обработка входящего сообщений **/
    private void processingTheMessage(Update update) throws TelegramApiException {
        String msg = update.getMessage().getText();

        deleteLastMessages(update);

        Message message = cmd_start(update);
        userService.registerMessage(message.getChatId(), message.getMessageId());
    }

    /** Удаление последних сообщений **/
    private void deleteLastMessages(Update update) {
        userService.registerMessage(userParameter.getChatId(update), userParameter.getMessage(update));
        userService.deleteOldMessages(update);
    }

    /** Обработка входящего сигнала **/
    private void processingTheCallbackQuery(Update update) throws TelegramApiException {
        String data = update.getCallbackQuery().getData();
        List<Message> messages;

        System.out.println("processingTheCallBackQuery: " + data);

        switch (data) {
            case APARTMENTS -> messages = data_apartments(update);
            case RENT_AN_APARTMENT -> messages = data_rent_an_apartment(update);
            case FILL_OUT_AN_APPLICATION -> messages = data_fill_out_an_application(update);
            case HOUSE_INFORMATION -> messages = data_house_information(update);
            case CONTACTS -> messages = data_contacts(update);
            case RULES -> messages = data_rules(update);
            case CHANGE_LANGUAGE -> messages = data_change_language(update);
            case TR,
                 EN,
                 RU -> messages = changeLanguage(update, data);
            default -> messages = data_start(update);
        }

        for (Message message : messages) {
            if (message == null)
                continue;

            userService.registerMessage(message.getChatId(), message.getMessageId());
        }
    }

    /** Смена языка интерфейса пользователя **/
    private List<Message> changeLanguage(Update update, String language) throws TelegramApiException {
        userService.changeUserLanguage(update.getCallbackQuery().getMessage(), language);
        return data_start(update);
    }

    @Override
    public Message app_nextStep(Update update, int step) throws TelegramApiException {
        switch (step) {
            case 1 -> userService.setUserApplicationName(update.getMessage());
            case 2 -> userService.setUserApplicationCountOfPerson(update.getMessage());
            case 3 -> userService.setUserApplicationTimeRent(update.getMessage());
            case 4 -> {
                userService.setUserApplicationCommentary(update.getMessage());

                userService.sendUserApplicationToAdmin(update);

                return botConfig.getTelegramClient().execute(userService.buildSendMessage(update,
                        userService.getLocalizationText(update),
                        userService.getMainIKMarkup(update)));
            }
            default -> {
                return botConfig.getTelegramClient().execute(userService.buildSendMessage(update,
                        userService.getLocalizationText(update),
                        userService.getMainIKMarkup(update)));
            }
        }

        return botConfig.getTelegramClient().execute(userService.buildSendMessage(update,
                userService.getLocalizationText(update),
                userService.getBackIKMarkup(update, BACK)));
    }

    @Override
    public Message cmd_start(Update update) throws TelegramApiException {
        userService.authorization(update.getMessage());

        return botConfig.getTelegramClient().execute(userService.buildSendMessage(update,
                userService.getLocalizationText(update),
                userService.getMainIKMarkup(update)));
    }

    @Override
    public List<Message> data_start(Update update) throws TelegramApiException {
        return Collections.singletonList(
                botConfig.getTelegramClient().execute(userService.buildSendMessage(update,
                userService.getLocalizationText(update),
                userService.getMainIKMarkup(update))));
    }

    @Override
    public @NotNull List<Message> data_apartments(Update update) throws TelegramApiException {
        List<Message> messages = new ArrayList<>(
                botConfig.getTelegramClient().execute(
                userService.sendPhotos(update, "apartment")));

        messages.add(botConfig.getTelegramClient().execute(userService.buildSendMessage(update,
                userService.getLocalizationText(update),
                userService.getBackIKMarkup(update, BACK_TO_START))));

        return messages;
    }

    @Override
    public List<Message> data_rent_an_apartment(Update update) throws TelegramApiException {
        return Collections.singletonList(
                botConfig.getTelegramClient().execute(userService.buildSendMessage(update,
                userService.getLocalizationText(update),
                userService.getRentAnApartmentIKMarkup(update))));
    }

    @Override
    public List<Message> data_fill_out_an_application(Update update) throws TelegramApiException {
        userService.setUserFillingOutStep(userParameter.getChatId(update), 1);

        return Collections.singletonList(
                botConfig.getTelegramClient().execute(userService.buildSendMessage(update,
                userService.getLocalizationText(update),
                userService.getBackIKMarkup(update, RENT_AN_APARTMENT))));
    }

    @Override
    public List<Message> data_house_information(Update update) throws TelegramApiException {
        return Collections.singletonList(
                botConfig.getTelegramClient().execute(userService.buildSendMessage(update,
                userService.getLocalizationText(update),
                userService.getHouseInformationIKMarkup(update))));
    }

    @Override
    public List<Message> data_contacts(Update update) throws TelegramApiException {
        return Collections.singletonList(
                botConfig.getTelegramClient().execute(userService.buildSendMessage(update,
                userService.getLocalizationText(update),
                userService.getBackIKMarkup(update, BACK_TO_START))));
    }

    @Override
    public List<Message> data_rules(Update update) throws TelegramApiException {
        return Collections.singletonList(
                botConfig.getTelegramClient().execute(userService.buildSendMessage(update,
                userService.getLocalizationText(update),
                userService.getBackIKMarkup(update, HOUSE_INFORMATION))));
    }

    @Override
    public List<Message> data_change_language(Update update) throws TelegramApiException {
        return Collections.singletonList(
                botConfig.getTelegramClient().execute(userService.buildSendMessage(update,
                userService.getLocalizationText(update),
                userService.getChangeLanguageIKMarkup(update))));
    }
}
