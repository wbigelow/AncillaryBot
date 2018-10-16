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

/**
 * Manages the commands. Only modification to the file should be the SERVER_ID, MOD_ROLE_ID, and ADMIN_ROLE_ID values.
 */
public class CommandManager {
    private static final long SERVER_ID = 362689877269020684L; // REPLACE 0 WITH YOUR SERVER ID
    private static final long MOD_ROLE_ID = 363053253509775371L; // REPLACE 0 WITH THE MOD ROLE ON YOUR SERVER (OR ADMIN IF NO MOD).
    private static final long ADMIN_ROLE_ID = 459560475034648616L; // REPLACE 0 WITH THE ADMIN ROLE ON YOUR SERVER.
    /**
     * Maps command trigger words to the command.
     */
    private final Map<String, Command> commands;

    /**
     * Constructor. Grabs all the commands from the modules and adds them to the help list.
     * @param modules The modules to get commands from.
     */
    public CommandManager(final List<Module> modules) {
        final ImmutableMap.Builder<String, Command> mapBuilder = ImmutableMap.builder();
        modules.forEach(module -> module.getCommands().forEach(command -> mapBuilder.put(command.getName().toLowerCase(), command)));
        final HelpCommand helpCommand = new HelpCommand();
        mapBuilder.put(helpCommand.getName(), helpCommand);
        commands = mapBuilder.build();
    }

    /**
     * Checks if a command is invokable by the user and runs the command if so.
     * @param commandWord The command word which was used by the user.
     * @param message The command's message object.
     * @param discordApi The discordApi client.
     */
    public void executeCommand(final String commandWord, final Message message, final DiscordApi discordApi) {
        final User user = message.getAuthor().asUser().get();
        if (!user.isBot() && commands.containsKey(commandWord.toLowerCase())) {
            final Command command = commands.get(commandWord.toLowerCase());
            final Server server = discordApi.getServerById(SERVER_ID).get();
            final List<Role> userRoles = user.getRoles(server);
            final Role modRole = server.getRoleById(MOD_ROLE_ID).get();
            final Role adminRole = server.getRoleById(ADMIN_ROLE_ID).get();
            if (command.getRequiredPermissionLevel().getLevel() <=
                    ((userRoles.contains(adminRole) ? 2 : 0) + (userRoles.contains(modRole) ? 1 : 0))) {
                command.execute(message, discordApi);
            } else {
                new MessageBuilder()
                        .setContent("You do not have permission to run this command.")
                        .send(message.getChannel());
            }
        }
    }

    /**
     * Command without a module. This is only done to allow for the help command to access the list of all commands added.
     */
    final class HelpCommand implements Command {

        @Override
        public String getName() {
            return "help";
        }

        @Override
        public String getDescription() {
            return "Messages the user with all the commands the bot has.";
        }

        @Override
        public PermissionLevel getRequiredPermissionLevel() {
            return PermissionLevel.ANY;
        }

        @Override
        public void execute(final Message message, final DiscordApi discordApi) {
            final EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setTitle("Here are all the commands the bot can do.")
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
