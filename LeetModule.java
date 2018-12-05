// This module was made by OwenTheGoat (alias +updog)

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
public class LeetModule implements Module {

    // Add to the commands
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
            return "Replies \"leet -> 1337\".";
        }

        @Override
        public PermissionLevel getRequiredPermissionLevel() {
            return PermissionLevel.ANY;
        }

        private String translateLeet(final Message message) {
            final String content = message.getContent();
            String leet = "";
            if (content.length() < 6) {
                return "t00 sh0r7";
            }
            // Start at 6 for ">leet _______"
            for (int i = 5; i < content.length(); i++) {
                char c = Character.toLowerCase(content.charAt(i));
                switch (c) {
                    case 'o': leet += "0";
                        break;
                    case 'l': leet += "1";
                        break;
                    case 'e': leet += "3";
                        break;
                    case 'a': leet += "4";
                        break;
                    case '2': leet += "Z";
                        break;
                    default: leet += c;
                        break;
                }
            }
            return leet;
        }

        @Override
        public void execute(final Message message, final DiscordApi discordApi) {
            new MessageBuilder()
                    .append(translateLeet(message))
                    .send(message.getChannel());
        }
    }
}
