package xao.develop.service.admin;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

@Service
public class AdminMsgEditAmenities extends AdminMessage {

    @Override
    protected InlineKeyboardMarkup getIKMarkup(long chatId) {
        return null;
    }
}
