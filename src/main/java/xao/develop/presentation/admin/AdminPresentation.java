package xao.develop.presentation.admin;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import xao.develop.presentation.Account;

@Component
public class AdminPresentation implements Account, Admin {
    @Override
    public void core(Update update) {

    }
}
