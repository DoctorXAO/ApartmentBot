package xao.develop.client.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import xao.develop.client.Account;
import xao.develop.service.user.UserService;

@Slf4j
@Component
public class UserClient implements Account {

    @Autowired
    UserService userService;

    @Override
    public void core(Update update) {
        log.trace("Method core(Update) started");

        userService.execute(update);

        log.trace("Method core(Update) finished");
    }
}
