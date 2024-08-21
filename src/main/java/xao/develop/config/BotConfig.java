package xao.develop.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import xao.develop.service.CommandService;

@Configuration
@PropertySource("application.properties")
@Getter
public class BotConfig {
    @Value("${bot.token}")
    private String token;
    @Value("${bot.token}")
    private OkHttpTelegramClient telegramClient;

    @Bean
    public TelegramBotsLongPollingApplication telegramBotsLongPollingApplication() {
        return new TelegramBotsLongPollingApplication();
    }

    @Bean
    public CommandService commandService() {
        return new CommandService();
    }
}
