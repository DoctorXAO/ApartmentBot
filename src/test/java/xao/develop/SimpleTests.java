package xao.develop;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.util.Arrays;

public class SimpleTests {
    @Test
    void checkTest() throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource("img/apartments");

        if (resource == null)
            throw new Exception("Directory with photos isn't found!");

        String[] files = new File(resource.getFile()).list();

        Arrays.sort(files, String::compareToIgnoreCase);

        for (String file : files) {
            System.out.println(file);
        }
    }
}
