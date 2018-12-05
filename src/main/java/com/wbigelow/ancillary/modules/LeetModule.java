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
 * @author OwenTheGoat (alias +updog)
 */
@NoArgsConstructor
public class LeetModule implements Module {

    @Override
    public List<Command> getCommands() {
        return ImmutableList.of(
                new LeetCommand()
        );
    }

    @NoArgsConstructor
    final class LeetCommand implements Command {

        @Override
        public String getName() {
            return "leet";
        }

        @Override
        public String getDescription() {
            return "Changes all text to leetspeak.";
        }

        @Override
        public PermissionLevel getRequiredPermissionLevel() {
            return PermissionLevel.ANY;
        }

        private String translateLeet(final Message message) {
            final String content = message.getContent();
            final StringBuilder response = new StringBuilder();
            if (content.length() < getName().length() + 2) {
                return "t00 sh0r7";
            }
            // Start at 6 for command name
            for (int i = 5; i < content.length(); i++) {
                final char c = Character.toLowerCase(content.charAt(i));
                switch (c) {
                    case 'o': response.append("0");
                        break;
                    case 'l': response.append("1");
                        break;
                    case 'e': response.append("3");
                        break;
                    case 'a': response.append("4");
                        break;
                    case '2': response.append("Z");
                        break;
                    case 't': response.append("7");
                        break;
                    default: response.append(c);
                        break;
                }
            }
            return response.toString();
        }

        @Override
        public void execute(final Message message, final DiscordApi discordApi) {
            new MessageBuilder()
                    .append(translateLeet(message))
                    .send(message.getChannel());
        }
    }
}
