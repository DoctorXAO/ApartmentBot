package xao.develop.view.User;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

public interface User {
    Message app_nextStep(Update update, String msg) throws TelegramApiException;

    Message cmd_start(Update update) throws TelegramApiException;

    List<Message> data_start(Update update) throws TelegramApiException;
    List<Message> data_apartments(Update update) throws TelegramApiException;
    List<Message> data_rent_an_apartment(Update update) throws TelegramApiException;
    List<Message> data_fill_out_an_application(Update update) throws TelegramApiException;
    List<Message> data_house_information(Update update) throws TelegramApiException;
    List<Message> data_contacts(Update update) throws TelegramApiException;
    List<Message> data_rules(Update update) throws TelegramApiException;
    List<Message> data_change_language(Update update) throws TelegramApiException;
}
