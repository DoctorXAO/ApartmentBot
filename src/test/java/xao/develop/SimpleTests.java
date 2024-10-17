package xao.develop;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import xao.develop.toolbox.PropertiesManager;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

@Slf4j
public class SimpleTests {

    @Test
    void checkTest() {
        check("languages/amenity_ru.properties", "amenities.viva-la", "lik-mo");
    }

    void check(String filePath, String key, String value) {
        Properties properties = new Properties();

        String path = PropertiesManager.class.getClassLoader().getResource(filePath).getPath();

        try (FileInputStream inputStream = new FileInputStream(path)) {
            properties.load(inputStream);

            log.debug("Property loaded by path {} successfully!", path);
        } catch (IOException ex) {
            log.debug("Can't load property by path {}\nException: {}", path, ex.getMessage());
        }

        properties.setProperty(key, value);

        try (FileOutputStream outputStream = new FileOutputStream(path)) {
            properties.store(outputStream, null);

            log.debug("Property added or updated by key {} and value {} successfully!", key, value);
        } catch (IOException ex) {
            log.error("Can't add or update property by key {} and value {}.\nException: {}", key, value, ex.getMessage());
        }
    }
}
