package xao.develop.service.user;

import xao.develop.config.UserCommand;
import xao.develop.config.UserMessageLink;
import xao.develop.service.BotMessage;

public abstract class UserMessage extends BotMessage implements UserCommand, UserMessageLink {

}
