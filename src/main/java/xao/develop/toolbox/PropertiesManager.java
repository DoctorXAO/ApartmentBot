package xao.develop.toolbox;

import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

@Slf4j
public class PropertiesManager {

    /** Add or update property **/
    public static void addOrUpdateProperty(String filePath, String key, String value) {
        Properties properties = new Properties();

        URL url = PropertiesManager.class.getClassLoader().getResource(filePath);

        if (url != null) {
            String path = url.getPath();

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
        } else
            log.error("Can't load resources by path {}", filePath);
    }

    /** Remove property **/
    public static void removeProperty(String filePath, String key) {
        Properties properties = new Properties();

        try (FileInputStream inputStream = new FileInputStream(filePath);
             FileOutputStream outputStream = new FileOutputStream(filePath))
        {
            properties.load(inputStream);

            properties.remove(key);

            properties.store(outputStream, null);

            log.debug("Property removed by key {} successfully!", key);
        } catch (IOException ex) {
            log.error("Can't remove property by key {}\nException: {}", key, ex.getMessage());
        }
    }
}
