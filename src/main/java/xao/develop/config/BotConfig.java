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

    @Value("${rent.1.day}")
    private String onePerDay;
    @Value("${rent.2.day}")
    private String twoPerDay;
    @Value("${rent.3.day}")
    private String threePerDay;
    @Value("${rent.1.month}")
    private String onePerMonth;
    @Value("${rent.2.month}")
    private String twoPerMonth;
    @Value("${rent.3.month}")
    private String threePerMonth;
    @Value("${rent.1.year}")
    private String onePerYear;
    @Value("${rent.2.year}")
    private String twoPerYear;
    @Value("${rent.3.year}")
    private String threePerYear;

    @Bean
    public TelegramBotsLongPollingApplication telegramBotsLongPollingApplication() {
        return new TelegramBotsLongPollingApplication();
    }
}
