package com.wbigelow.ancillary;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;

/**
 * A command which can invoked.
 */
public interface Command {
    /**
     * Gets the name of the command.
     * @return the name of the command.
     */
    public String getName();

    /**
     * Gets the description of your command and what arguments it needs.
     * @return the description of the command.
     */
    public String getDescription();

    /**
     * Gets the required permission level to run the command.
     * @return the required permission level to run the command.
     */
    public PermissionLevel getRequiredPermissionLevel();

    /**
     * Executes the command and does the main action of the command.
     * @param message the Message object sent which triggered the command.
     * @param discordApi the discordApi which can be used to add roles, kick, ban, etc.
     */
    public void execute(Message message, DiscordApi discordApi);
}
