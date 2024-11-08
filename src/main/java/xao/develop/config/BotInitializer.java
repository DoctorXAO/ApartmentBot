package xao.develop.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import xao.develop.ApartmentBotApplication;

@Component
public class BotInitializer {

    @Autowired
    private BotConfig botConfig;

    @Autowired
    private ApartmentBotApplication application;

    @Autowired
    private TelegramBotsLongPollingApplication bot;

    @EventListener(ContextRefreshedEvent.class)
    public void init() throws TelegramApiException {
        bot.registerBot(botConfig.getToken(), application);
    }
}
