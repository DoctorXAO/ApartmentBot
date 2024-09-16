package xao.develop.server;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Service
public class Server {
    public Long getChatId(Update update) {
        log.trace("Method 'getChatId' started");

        Long chatId = update.hasMessage() ?
                update.getMessage().getChatId() :
                update.getCallbackQuery().getMessage().getChatId();

        log.trace("Method 'getChatId' finished and it's returning the next value: {}", chatId);

        return chatId;
    }

    public Integer getMessageId(Update update) {
        log.trace("Method 'getMsgId' started");

        Integer msgId = update.hasMessage() ?
                update.getMessage().getMessageId() :
                update.getCallbackQuery().getMessage().getMessageId();

        log.trace("Method 'getMsgId' finished and it's returning the next value: {}", msgId);

        return msgId;
    }
}
