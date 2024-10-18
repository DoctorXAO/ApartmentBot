package xao.develop.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

@Configuration
public class LocaleConfig {
    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();

        messageSource.setBasenames(
                "file:./config/languages/language",
                "file:./config/languages/amenity");

        messageSource.setDefaultEncoding("UTF-8");

        messageSource.setUseCodeAsDefaultMessage(true);

        messageSource.setCacheSeconds(60);

        return messageSource;
    }
}
