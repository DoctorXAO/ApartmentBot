package xao.develop;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class SimpleTests {

    @Test
    void checkTest() {
        int x = (int) Math.ceil((double) 10/22);
        System.out.println(x);
    }
}
