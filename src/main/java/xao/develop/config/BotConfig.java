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
    private int onePerDay;
    @Value("${rent.2.day}")
    private int twoPerDay;
    @Value("${rent.3.day}")
    private int threePerDay;
    @Value("${rent.1.month}")
    private int onePerMonth;
    @Value("${rent.2.month}")
    private int twoPerMonth;
    @Value("${rent.3.month}")
    private int threePerMonth;
    @Value("${rent.1.year}")
    private int onePerYear;
    @Value("${rent.2.year}")
    private int twoPerYear;
    @Value("${rent.3.year}")
    private int threePerYear;

    @Value("${check.hours.in}")
    private int checkInHours;
    @Value("${check.hours.out}")
    private int checkOutHours;

    @Value("${count.of.apps-on-page}")
    private int countOfAppsOnPage;
    @Value("${count.of.apartments-on-page}")
    private int countOfApartmentOnPage;
    @Value("${count.of.amenities-on-page}")
    private int countOfAmenitiesOnPage;

    public int getPerDay(int countOfPeople) {
        if (countOfPeople == 1)
            return onePerDay;
        else if (countOfPeople == 2)
            return twoPerDay;
        else
            return threePerDay;
    }

    public int getPerMonth(int countOfPeople) {
        if (countOfPeople == 1)
            return onePerMonth;
        else if (countOfPeople == 2)
            return twoPerMonth;
        else
            return threePerMonth;
    }

    public int getPerYear(int countOfPeople) {
        if (countOfPeople == 1)
            return onePerYear;
        else if (countOfPeople == 2)
            return twoPerYear;
        else
            return threePerYear;
    }

    @Bean
    public TelegramBotsLongPollingApplication telegramBotsLongPollingApplication() {
        return new TelegramBotsLongPollingApplication();
    }
}
