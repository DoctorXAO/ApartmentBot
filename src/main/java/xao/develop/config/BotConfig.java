package xao.develop.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;

@Getter
public class BotConfig {
    @Value("${bot.token}")
    private String token;
}
