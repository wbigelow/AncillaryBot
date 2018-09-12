package com.wbigelow.ancillary;

import com.google.common.collect.ImmutableMap;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

import java.awt.*;
import java.util.List;
import java.util.Map;

public class CommandManager {
    private static final long UW_SERVER_ID = 362689877269020684L;
    private static final long MOD_ROLE_ID = 363053253509775371L;
    private static final long ADMIN_ROLE_ID = 459560475034648616L;
    /**
     * Maps command trigger words to the command.
     */
    private final Map<String, Command> commands;

    public CommandManager(final List<Module> modules) {
        final ImmutableMap.Builder<String, Command> mapBuilder = ImmutableMap.builder();
        modules.forEach(module -> module.getCommands().forEach(command -> mapBuilder.put(command.getName().toLowerCase(), command)));
        final HelpCommand helpCommand = new HelpCommand();
        mapBuilder.put(helpCommand.getName(), helpCommand);
        commands = mapBuilder.build();
    }

    public void executeCommand(final String commandWord, final Message message, final DiscordApi discordApi) {
        final User user = message.getAuthor().asUser().get();
        if (!user.isBot() && commands.containsKey(commandWord.toLowerCase())) {
            final Command command = commands.get(commandWord.toLowerCase());
            final Server server = discordApi.getServerById(UW_SERVER_ID).get();
            final List<Role> userRoles = user.getRoles(server);
            final Role modRole = server.getRoleById(MOD_ROLE_ID).get();
            final Role adminRole = server.getRoleById(ADMIN_ROLE_ID).get();
            switch (command.getRequiredPermissionLevel()) {
                case ANY:
                    command.execute(message, discordApi);
                    break;
                case MOD:
                    if (userRoles.contains(modRole) || userRoles.contains(adminRole)) {
                        command.execute(message, discordApi);
                    }
                    break;
                case ADMIN:
                    if (userRoles.contains(adminRole)) {
                        command.execute(message, discordApi);
                    }
                    break;
                default:
                    // No-Op
                    break;
            }
        }
    }

    final class HelpCommand implements Command {

        @Override
        public String getName() {
            return "help";
        }

        @Override
        public String getDescription() {
            return "Messages the user with all the commands ancillary has.";
        }

        @Override
        public PermissionLevel getRequiredPermissionLevel() {
            return PermissionLevel.ANY;
        }

        @Override
        public void execute(final Message message, final DiscordApi discordApi) {
            final EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setTitle("Here are all the commands Ancillary can do.")
                    .setColor(Color.GREEN);
            for (final Command command : commands.values()) {
                embedBuilder.addField(command.getName(), command.getDescription());
            }
            new MessageBuilder()
                    .setEmbed(embedBuilder)
                    .send(message.getAuthor().asUser().get());
        }
    }
}
