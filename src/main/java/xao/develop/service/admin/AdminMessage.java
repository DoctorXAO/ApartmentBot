package xao.develop.service.admin;

import lombok.extern.slf4j.Slf4j;
import xao.develop.config.AdminCommand;
import xao.develop.config.AdminMessageLink;
import xao.develop.service.BotMessage;

@Slf4j
public abstract class AdminMessage extends BotMessage implements AdminCommand, AdminMessageLink {

}
