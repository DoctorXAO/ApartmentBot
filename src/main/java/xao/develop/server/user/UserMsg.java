package xao.develop.server.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import xao.develop.config.BotConfig;
import xao.develop.config.UserCommand;
import xao.develop.server.BotMessage;
import xao.develop.server.MessageBuilder;

@Service
public abstract class UserMsg implements BotMessage, UserCommand {
    @Autowired
    BotConfig botConfig;

    @Autowired
    MessageBuilder msgBuilder;

    @Autowired
    UserLocalization userLoc;

    @Override
    public Message sendMessage(Update update) throws TelegramApiException {
        return botConfig.getTelegramClient().execute(msgBuilder.buildSendMessage(update,
                userLoc.getLocalizationText(update),
                getIKMarkup(update)));
    }
}
