package xao.develop.server.user;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

@Scope("prototype")
@Service
public class UserMsgChangeCheckInMonth extends UserMsg {

    @Override
    public InlineKeyboardMarkup getIKMarkup(Update update) {
        return null;
    }
}
