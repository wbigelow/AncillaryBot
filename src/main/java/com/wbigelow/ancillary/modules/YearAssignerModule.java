package com.wbigelow.ancillary.modules;

import com.vdurmont.emoji.EmojiParser;
import com.wbigelow.ancillary.Command;
import com.wbigelow.ancillary.Module;
import com.wbigelow.ancillary.PermissionLevel;
import lombok.NoArgsConstructor;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;

import java.awt.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class YearAssignerModule implements Module {

    private final Map<String, Long> roles = new HashMap<>();

    @Override
    public List<Command> getCommands() {
        roles.put("Freshmen", 437466872447893504L);
        roles.put("Sophomores", 452303996640821248L);
        roles.put("Juniors", 452304063304957952L);
        roles.put("Seniors", 452304274735497228L);
        roles.put("Alumni", 451857408692715552L);
        roles.put("Prospective", 414579254211117057L);
        roles.put("Grad", 461818517600206868L);
        roles.put("TS", 452344486098632722L);
//        return ImmutableList.of(
//                new YearCommand()
//        );
        return Collections.emptyList();
    }

    @NoArgsConstructor
    final class YearCommand implements Command {

        @Override
        public String getName() {
            return "setyear";
        }

        @Override
        public String getDescription() {
            return "Allows a user to set their year.";
        }

        @Override
        public PermissionLevel getRequiredPermissionLevel() {
            return PermissionLevel.ANY;
        }

        @Override
        public void execute(final Message message, final DiscordApi discordApi) {
            try {
                final Message sentMessage = new MessageBuilder()
                        .setEmbed(new EmbedBuilder()
                            .setTitle("Select your year!")
                            .addField("Instructions", "Click the reaction which corresponds to your year to be assigned to it.")
                            .addField("Freshmen", ":one:")
                            .addField("Sophomore", ":two:")
                            .addField("Junior", ":three:")
                            .addField("Senior", ":four:")
                            .addField("Graduate Student", ":five:")
                            .addField("Alum", ":six:")
                            .addField("TS", ":seven:")
                            .addField("Prospective", ":eight:")
                            .setColor(Color.CYAN))
                        .send(message.getChannel())
                        .get(1, TimeUnit.SECONDS);
                sentMessage.addReactions(
                        EmojiParser.parseToUnicode(":one:"),
                        EmojiParser.parseToUnicode(":two:"),
                        EmojiParser.parseToUnicode(":three:"),
                        EmojiParser.parseToUnicode(":four:"),
                        EmojiParser.parseToUnicode(":five:"),
                        EmojiParser.parseToUnicode(":six:"),
                        EmojiParser.parseToUnicode(":seven:"),
                        EmojiParser.parseToUnicode(":eight:"));
                sentMessage.addReactionAddListener(event -> {
                    final User user = event.getUser();
                    if (!user.isBot()) {
                        String year = "";
                        if (event.getEmoji().equalsEmoji(EmojiParser.parseToUnicode(":one:"))) {
                            year = "Freshmen";
                        }
                        if (event.getEmoji().equalsEmoji(EmojiParser.parseToUnicode(":two:"))) {
                            year = "Sophomores";
                        }
                        if (event.getEmoji().equalsEmoji(EmojiParser.parseToUnicode(":three:"))) {
                            year = "Juniors";
                        }
                        if (event.getEmoji().equalsEmoji(EmojiParser.parseToUnicode(":four:"))) {
                            year = "Seniors";
                        }
                        if (event.getEmoji().equalsEmoji(EmojiParser.parseToUnicode(":five:"))) {
                            year = "Grad";
                        }
                        if (event.getEmoji().equalsEmoji(EmojiParser.parseToUnicode(":six:"))) {
                            year = "Almuni";
                        }
                        if (event.getEmoji().equalsEmoji(EmojiParser.parseToUnicode(":seven:"))) {
                            year = "TS";
                        }
                        if (event.getEmoji().equalsEmoji(EmojiParser.parseToUnicode(":eight:"))) {
                            year = "Prospective";
                        }
                        for (final String val : roles.keySet()) {
                            if (year.equals(val)) {
                                discordApi.getRoleById(roles.get(val)).ifPresent(user::addRole);
                            } else {
                                discordApi.getRoleById(roles.get(val)).ifPresent(user::removeRole);
                            }
                        }
                        if (user.getRoles(message.getServer().get()).contains(discordApi.getRoleById(452308369961648128L).get())) {
                            discordApi.getRoleById(452308369961648128L).ifPresent(user::removeRole); // Just Joined Role
                            discordApi.getRoleById(452272203078172692L).ifPresent(user::addRole); // New Role
                            discordApi.getTextChannelById(362689877751627777L).ifPresent(channel -> {
                                new MessageBuilder()
                                        .setContent(user.getMentionTag() + " just joined the server!")
                                        .send(channel);
                            });
                        }
                        sentMessage.delete();
                    }
                });
                message.delete();
            } catch (final InterruptedException | ExecutionException | TimeoutException e) {
                e.printStackTrace();
            }
        }
    }
}
