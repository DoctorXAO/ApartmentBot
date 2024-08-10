package xao.develop;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import xao.develop.config.BotConfig;
import xao.develop.config.SpringConfig;

public class Main {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(SpringConfig.class);

        BotConfig botConfig = context.getBean("botConfigBean", BotConfig.class);
        ApartmentBot apartmentBot = context.getBean("apartmentBotBean", ApartmentBot.class);

        try {
            TelegramBotsLongPollingApplication bot = new TelegramBotsLongPollingApplication();
            bot.registerBot(botConfig.getToken(), apartmentBot);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        } finally {
            context.close();
        }
    }
}
