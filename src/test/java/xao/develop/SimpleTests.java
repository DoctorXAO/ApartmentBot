package xao.develop;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import xao.develop.enums.UserStep;

@Slf4j
public class SimpleTests {

    @Test
    void checkTest() {
        UserStep step = UserStep.valueOf("empty".toUpperCase());
        System.out.println(step);
    }
}
