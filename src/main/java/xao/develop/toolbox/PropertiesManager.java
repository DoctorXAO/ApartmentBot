package xao.develop.toolbox;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

@Slf4j
public class PropertiesManager {

    public static String getPropertyValue(String filePath, String key) {
        Properties properties = new Properties();

        loadProperty(properties, filePath);

        return properties.getProperty(key);
    }

    /** Add or update property **/
    public static void addOrUpdateProperty(String filePath, String key, String value) {
        Properties properties = new Properties();

        loadProperty(properties, filePath);

        properties.setProperty(key, value);

        try (Writer writer = new OutputStreamWriter(new FileOutputStream(filePath), StandardCharsets.UTF_8)) {
            properties.store(writer, null);

            log.debug("Property added or updated by key {} and value {} successfully!", key, value);
        } catch (IOException ex) {
            log.error("Can't add or update property by key {} and value {}.\nException: {}", key, value, ex.getMessage());
        }
    }

    /** Remove property **/
    public static void removeProperty(String filePath, String key) {
        Properties properties = new Properties();

        loadProperty(properties, filePath);

        properties.remove(key);

        try (FileOutputStream outputStream = new FileOutputStream(filePath)) {
            properties.store(outputStream, null);

            log.debug("Property removed by key {} successfully!", key);
        } catch (IOException ex) {
            log.error("Can't remove property by key {}\nException: {}", key, ex.getMessage());
        }
    }

    private static void loadProperty(Properties properties, String path) {
        try (Reader reader = new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8)) {
            properties.load(reader);

            log.debug("Property loaded by path {} successfully!", path);
        } catch (IOException ex) {
            log.debug("Can't load property by path {}\nException: {}", path, ex.getMessage());
        }
    }
}
