package xao.develop.config;

import lombok.Getter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import xao.develop.ApartmentBot;

@Configuration
@PropertySource("application.properties")
@Getter
public class SpringConfig {

    @Bean("botConfigBean")
    BotConfig botConfig() {
        return new BotConfig();
    }

    @Bean("apartmentBotBean")
    ApartmentBot apartmentBot() { return new ApartmentBot(); }
}
