package xao.develop.client.admin;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import xao.develop.client.Account;
import xao.develop.service.admin.AdminService;

@Slf4j
@Component
public class AdminClient implements Account {

    @Autowired
    AdminService adminService;

    @Override
    public void core(Update update) {
        log.trace("Method core(Update) started");

        adminService.execute(update);

        log.trace("Method core(Update) finished");
    }
}
