package xao.develop.service.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

@Slf4j
@Service
public class UserMsgPreviewCard extends UserMessage {

    @Override
    public InlineKeyboardMarkup getIKMarkup(long chatId) {
        return InlineKeyboardMarkup
                .builder()
                .keyboardRow(new InlineKeyboardRow(
                        msgBuilder.buildIKButton(service.getLocaleMessage(chatId, USER_BT_SEND),
                                RAA_SEND_BOOKING_TO_ADMIN)))
                .keyboardRow(new InlineKeyboardRow(
                        msgBuilder.buildIKButton(service.getLocaleMessage(chatId, GENERAL_BT_BACK),
                                RAA_QUIT_FROM_PREVIEW_CARD)))
                .build();
    }
}
