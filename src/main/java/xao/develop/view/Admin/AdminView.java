package xao.develop.view.Admin;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import xao.develop.view.Account;

@Component
public class AdminView implements Account, Admin {
    @Override
    public void core(Update update) {

    }
}
