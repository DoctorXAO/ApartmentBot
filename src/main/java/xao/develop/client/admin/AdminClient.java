package xao.develop.client.admin;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import xao.develop.client.Account;

@Component
public class AdminClient implements Account, Admin {
    @Override
    public void core(Update update) {

    }
}
