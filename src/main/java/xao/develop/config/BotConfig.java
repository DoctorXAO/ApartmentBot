package xao.develop.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

@Configuration
@PropertySource("application.properties")
@Getter
public class BotConfig {
    @Value("${bot.token}")
    private String token;
    @Value("${bot.token}")
    private OkHttpTelegramClient telegramClient;
    @Value("${owner.phone}")
    private String phone;
    @Value("${owner.email}")
    private String email;
    @Value("${bot.adminId}")
    private Long adminId;

    @Bean
    public TelegramBotsLongPollingApplication telegramBotsLongPollingApplication() {
        return new TelegramBotsLongPollingApplication();
    }
}
