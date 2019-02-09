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

@NoArgsConstructor
public class PingModule implements Module {
    @Override
    public List<Command> getCommands() {
        return ImmutableList.of(
                new PingCommand()
        );
    }

    @NoArgsConstructor
    final class PingCommand implements Command {

        @Override
        public String getName() {
            return "ping";
        }

        @Override
        public String getDescription() {
            return "Replies \"Pong\".";
        }

        @Override
        public PermissionLevel getRequiredPermissionLevel() {
            return PermissionLevel.ANY;
        }

        @Override
        public void execute(final Message message, final DiscordApi discordApi) {
            new MessageBuilder()
                    .append("Pong")
                    .send(message.getChannel());
        }
    }
}
