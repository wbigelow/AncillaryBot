package com.wbigelow.ancillary.modules;

import com.google.common.collect.ImmutableList;
import com.wbigelow.ancillary.Command;
import com.wbigelow.ancillary.Module;
import com.wbigelow.ancillary.PermissionLevel;
import lombok.NoArgsConstructor;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;

import java.util.List;

/**
 * Simple module which holds a command which sends "Pong".
 */
@NoArgsConstructor
public class PingModule implements Module { // All modules must implement the Module interface.
    @Override
    public List<Command> getCommands() {
        return ImmutableList.of(
                new PingCommand()  // This is a list of all the commands in the module.
        );
    }

    /**
     * A command which uses the trigger word "ping" to send "pong" back.
     */
    @NoArgsConstructor
    final class PingCommand implements Command {

        @Override
        public String getName() {
            return "ping"; // The command word which invokes this command.
        }

        @Override
        public String getDescription() {
            return "Replies \"Pong\"."; // The description of the command which appears in the help message.
        }

        @Override
        public PermissionLevel getRequiredPermissionLevel() {
            return PermissionLevel.ANY; // Permission level required to run the command.
        }

        @Override
        public void execute(final Message message, final DiscordApi discordApi) {
            // This creates a new message, sets its content to be "Pong", and then sends it to the channel
            // that the command was invoked in.
            new MessageBuilder()
                    .append("Pong")
                    .send(message.getChannel());
        }
    }
}
