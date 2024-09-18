package xao.develop.client.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import xao.develop.config.UserCommand;
import xao.develop.client.Account;
import xao.develop.server.user.UserServer;

@Slf4j
@Component
public class UserClient implements Account, UserCommand {

    @Autowired
    UserServer userServer;

    @Override
    public void core(Update update) {
        log.trace("Method core(Update) started");

        userServer.execute(update);

        log.trace("Method core(Update) finished");
    }
}
