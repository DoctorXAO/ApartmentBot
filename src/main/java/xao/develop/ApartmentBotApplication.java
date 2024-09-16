package xao.develop;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;
import xao.develop.config.BotConfig;
import xao.develop.client.admin.AdminClient;
import xao.develop.client.user.UserClient;

@Slf4j
@SpringBootApplication
public class ApartmentBotApplication implements LongPollingSingleThreadUpdateConsumer {
    @Autowired
    private BotConfig botConfig;

    @Autowired
    private UserClient user;

    @Autowired
    private AdminClient admin;

    public static void main(String[] args) {
        SpringApplication.run(ApartmentBotApplication.class, args);
    }

    @Override
    public void consume(Update update) {
        long chatId = update.hasMessage() ?
                update.getMessage().getChatId() :
                update.getCallbackQuery().getMessage().getChatId();

        if (chatId == botConfig.getAdminId())
            admin.core(update);
        else
            user.core(update);
    }
}
