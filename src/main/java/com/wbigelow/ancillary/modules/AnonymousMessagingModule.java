package com.wbigelow.ancillary.modules;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.wbigelow.ancillary.Command;
import com.wbigelow.ancillary.Module;
import com.wbigelow.ancillary.PermissionLevel;
import lombok.NoArgsConstructor;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.message.MessageBuilder;

import java.util.*;
import java.util.stream.*;

@NoArgsConstructor
public class AnonymousMessagingModule implements Module {
    /**
     * Random number generator.
     */
    private static final Random RANDOM = new Random();
    /**
     * The maximum number anonymous IDs can go up to.
     */
    private static final int MAX_ID = 1000;
    private final BiMap<MessageAuthor, Integer> anonIDs = HashBiMap.create();
    private final List<MessageAuthor> blacklist = new ArrayList<>();

    @Override
    public List<Command> getCommands() {
        return ImmutableList.of(
                new SendAnonymousMessageCommand(),
                new GetNewIDCommand(),
                new SendMessageToAnonymousUserCommand(),
                new BlacklistIDCommand(),
                new SendRelationshipsAnonymousMessageCommand(),
                new UnBlacklistIDCommand()
        );
    }

    /**
     * Creates an anonymous ID for a user.
     * @param user the user to create the ID for.
     * @return the created ID.
     */
    private int createAnonIDForUser(final MessageAuthor user) {
        int userID;
        if (anonIDs.keySet().size() > MAX_ID) { // We've filled up on IDs presumably
            // Just make the ID the lowest available ID
            userID = IntStream
                    .range(0, anonIDs.keySet().size())
                    .parallel()
                    .filter(x -> !anonIDs.values().contains(x))
                    .min()
                    .orElse(anonIDs.keySet().size());
        } else { // Otherwise give them a nice random one in our defined range
            userID = RANDOM.nextInt(MAX_ID);
            while (anonIDs.inverse().containsKey(userID)) {
                userID = (userID + 1) % MAX_ID; // Linear probing to find an unused ID.
            }
        }
        anonIDs.put(user, userID);
        new MessageBuilder()
                .setContent("You are now sending messages under the ID: `" + userID + "`.")
                .send(user.asUser().get());
        return userID;
    }

    private void sendAnonMessage(final Message message, final DiscordApi discordApi, final String channel) {
        final Iterator<ServerTextChannel> channels = discordApi.getServerTextChannelsByName(channel).iterator();
        final String content = message.getContent();
        final String anonymousMessage = content.contains(" ") ? content.substring(content.indexOf(" ") + 1) : "";
        final int anonID;
        final MessageAuthor author = message.getAuthor();
        if (anonIDs.containsKey(author)) {
            anonID = anonIDs.get(message.getAuthor());
        } else {
            anonID = createAnonIDForUser(author);
        }
        if (anonymousMessage.length() > 0 && channels.hasNext() && !blacklist.contains(author)) {
            new MessageBuilder()
                    .append("`" + anonID + "` ")
                    .append(anonymousMessage)
                    .send(channels.next());
        }
        if (message.canYouDelete()) {
            message.delete();
        }
    }

    final class SendAnonymousMessageCommand implements Command {

        @Override
        public String getName() {
            return "anon";
        }

        @Override
        public String getDescription() {
            return "Sends an anonymous message to the #anonymous channel under a random ID. "
                    + "Anything after the command word will be sent.";
        }

        @Override
        public PermissionLevel getRequiredPermissionLevel() {
            return PermissionLevel.ANY;
        }

        @Override
        public void execute(final Message message, final DiscordApi discordApi) {
            sendAnonMessage(message, discordApi, "anonymous");
        }
    }

    final class SendRelationshipsAnonymousMessageCommand implements Command {

        @Override
        public String getName() {
            return "relationships";
        }

        @Override
        public String getDescription() {
            return "Sends an anonymous message to the #relationships channel under a random ID. "
                    + "Anything after the command word will be sent.";
        }

        @Override
        public PermissionLevel getRequiredPermissionLevel() {
            return PermissionLevel.ANY;
        }

        @Override
        public void execute(final Message message, final DiscordApi discordApi) {
            sendAnonMessage(message, discordApi, "relationships");
        }
    }

    final class GetNewIDCommand implements Command {

        @Override
        public String getName() {
            return "newID";
        }

        @Override
        public String getDescription() {
            return "Creates a new ID to send anonymous messages from.";
        }

        @Override
        public PermissionLevel getRequiredPermissionLevel() {
            return PermissionLevel.ANY;
        }

        @Override
        public void execute(final Message message, final DiscordApi discordApi) {
            final MessageAuthor author = message.getAuthor();
            anonIDs.remove(author);
            createAnonIDForUser(author);
        }
    }

    final class SendMessageToAnonymousUserCommand implements Command {

        @Override
        public String getName() {
            return "message";
        }

        @Override
        public String getDescription() {
            return "Sends a message to the ID provided. Ex: " + getName() + " 999 hello";
        }

        @Override
        public PermissionLevel getRequiredPermissionLevel() {
            return PermissionLevel.ANY;
        }

        @Override
        public void execute(final Message message, final DiscordApi discordApi) {
            final MessageAuthor author = message.getAuthor();
            if (!anonIDs.containsKey(author)) {
                createAnonIDForUser(author);
            }
            final int authorID = anonIDs.get(author);
            final String[] content = message.getContent().split(" ", 3);
            if (content.length == 3) {
                try {
                    final int receiver = Integer.parseInt(content[1]);
                    if (anonIDs.inverse().containsKey(receiver)) {
                    new MessageBuilder()
                            .append("`" + authorID + "` ")
                            .append(content[2])
                            .send(anonIDs.inverse().get(receiver).asUser().get());
                    } else {
                    new MessageBuilder()
                            .append("Could not find a user under that ID.")
                            .send(message.getAuthor().asUser().get());
                    }
                } catch (final NumberFormatException e) {
                    // No-Op
                }
            }
        }
    }

    final class BlacklistIDCommand implements Command {

        @Override
        public String getName() {
            return "blacklist";
        }

        @Override
        public String getDescription() {
            return "Blacklists a user from sending anon messages. Requires mod permissions.";
        }

        @Override
        public PermissionLevel getRequiredPermissionLevel() {
            return PermissionLevel.MOD;
        }

        @Override
        public void execute(final Message message, final DiscordApi discordApi) {
            try {
                final int id = Integer.parseInt(message.getContent().split(" ", 2)[1]);
                blacklist.add(anonIDs.inverse().get(id));
                new MessageBuilder()
                        .setContent("User blacklisted.")
                        .send(message.getChannel());
            } catch (final NumberFormatException e) {
                new MessageBuilder()
                        .setContent("ID must be an integer.")
                        .send(message.getChannel());
            }
        }
    }

    final class UnBlacklistIDCommand implements Command {

        @Override
        public String getName() {
            return "unblacklist";
        }

        @Override
        public String getDescription() {
            return "Removes a user from the blacklist. Requires mod permissions.";
        }

        @Override
        public PermissionLevel getRequiredPermissionLevel() {
            return PermissionLevel.MOD;
        }

        @Override
        public void execute(final Message message, final DiscordApi discordApi) {
            try {
                final int id = Integer.parseInt(message.getContent().split(" ", 2)[1]);
                blacklist.remove(anonIDs.inverse().get(id));
                new MessageBuilder()
                        .setContent("User unblacklisted.")
                        .send(message.getChannel());
            } catch (final NumberFormatException e) {
                new MessageBuilder()
                        .setContent("ID must be an integer.")
                        .send(message.getChannel());
            }
        }
    }
}
