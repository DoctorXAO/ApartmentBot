package xao.develop;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import xao.develop.toolbox.LibreTranslateService;

@Slf4j
public class SimpleTests {

    @Test
    void checkTest() throws Exception {
        LibreTranslateService libreTranslateService = new LibreTranslateService();

        System.out.println(libreTranslateService.translate("house", "en", "tr"));
    }
}
