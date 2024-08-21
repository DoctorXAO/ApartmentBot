package xao.develop.service;

import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethodMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public interface BotService {
    Message cmd_start(Update update) throws TelegramApiException;

    Message data_apartments(Update update) throws TelegramApiException;
    Message data_rent_an_apartment(Update update) throws TelegramApiException;
    Message data_house_information(Update update) throws TelegramApiException;
    Message data_contacts(Update update) throws TelegramApiException;
    Message data_rules(Update update) throws TelegramApiException;
    Message dataIsNotRecognized();
}
