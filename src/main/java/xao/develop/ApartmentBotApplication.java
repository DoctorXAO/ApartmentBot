package xao.develop;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;
import xao.develop.config.BotConfig;
import xao.develop.view.Admin.AdminView;
import xao.develop.view.User.UserView;

@SpringBootApplication
public class ApartmentBotApplication implements LongPollingSingleThreadUpdateConsumer {
    @Autowired
    private BotConfig botConfig;

    @Autowired
    private UserView userView;

    @Autowired
    private AdminView adminView;

    public static void main(String[] args) {
        SpringApplication.run(ApartmentBotApplication.class, args);
    }

    @Override
    public void consume(Update update) {
        long chatId = update.hasMessage() ?
                update.getMessage().getChatId() :
                update.getCallbackQuery().getMessage().getChatId();

        userView.core(update);

//        if (chatId == botConfig.getAdminId())
//            adminView.core(update);
//        else
//            userView.core(update);
    }
}
