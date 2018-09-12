package com.wbigelow.ancillary;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;

public interface Command {
    public String getName();
    public String getDescription();
    public PermissionLevel getRequiredPermissionLevel();
    public void execute(Message message, DiscordApi discordApi);
}
