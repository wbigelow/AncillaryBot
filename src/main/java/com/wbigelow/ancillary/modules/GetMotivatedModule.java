package com.wbigelow.ancillary.modules;

import com.google.common.collect.ImmutableList;
import com.wbigelow.ancillary.Command;
import com.wbigelow.ancillary.Module;
import com.wbigelow.ancillary.PermissionLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


@NoArgsConstructor
public class GetMotivatedModule implements Module {
    private static final AtomicInteger counter = new AtomicInteger();
    private static final Map<String, Session> sessions = new HashMap<>();

    @Override
    public List<Command> getCommands() {
        return ImmutableList.of(
                new StartWorkCommand(),
                new EndWorkCommand(),
                new LeaveWorkCommand(),
                new ModifyWorkCommand(),
                new DeleteWorkCommand(),
                new JoinWorkCommand(),
                new ListWorkCommand()
        );
    }

    /**
     * Starts a session timer. Alerts all users in a session upon break time and break completion.
     * @param session Session to start timer for.
     * @param channelToUpdate Channel to send update messages to.
     */
    private void startSessionTimer(final Session session, final TextChannel channelToUpdate) {
        final int sessionDuration = session.getSessionDuration();
        final int sessionBreak = session.getSessionBreak();
        final int sessionID = session.getSessionID();
        final int sessionsRemaining = session.getSessionsRemaining();
        new Thread(() -> {
            try {
                session.setTimeElapsed(0);
                while (session.getTimeElapsed() < session.getSessionDuration() && session.isEnabled()) {
                    Thread.sleep(TimeUnit.MINUTES.toMillis(1));
                    session.setTimeElapsed(session.getTimeElapsed() + 1);
                }
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
            final String sessionUpdate = "***Session " + sessionID + " update.*** ";
            String breakMessage = sessionUpdate;
            List<User> users = session.getMembers();
            for (final User user : users) {
                breakMessage += user.getNicknameMentionTag();
            }
            breakMessage += " Great work! Time to take a break and check in!";
            if (session.isEnabled()) {
                new MessageBuilder()
                        .setContent(breakMessage)
                        .send(channelToUpdate);
                try {
                    session.setTimeElapsed(0);
                    while (session.getTimeElapsed() < session.getSessionBreak() && session.isEnabled()) {
                        Thread.sleep(TimeUnit.MINUTES.toMillis(1));
                        session.setTimeElapsed(session.getTimeElapsed() + 1);
                    }
                } catch (final InterruptedException e) {
                    e.printStackTrace();
                }
                String breakOverMessage = sessionUpdate;
                users = session.getMembers();
                for (final User user : users) {
                    breakOverMessage += user.getNicknameMentionTag();
                }
                if (sessionsRemaining > 1) {
                    breakOverMessage += " Break time has ended now, time to go back to work.";
                    startSessionTimer(session, channelToUpdate);
                } else {
                    breakOverMessage += " You've completed all your sessions."
                            + "I hope you got your tasks completed. Please set another one if you need more time.";
                    session.setSessionsRemaining(sessionsRemaining - 1);
                }
                if (session.isEnabled()) {
                    new MessageBuilder()
                            .setContent(breakOverMessage)
                            .send(channelToUpdate);
                }
            }
        }).start();
    }

    @Getter
    @Builder
    static class Session {
        final int sessionID;
        final User creator;
        final List<User> members;
        @Setter
        int sessionsRemaining;
        @Setter
        int sessionDuration;
        @Setter
        int sessionBreak;
        @Setter
        boolean isEnabled;
        @Setter
        int timeElapsed;
    }

    /**
     * Creates a session.
     */
    final class StartWorkCommand implements Command {

        @Override
        public String getName() {
            return "startwork";
        }

        @Override
        public String getDescription() {
            return "Starts a tasktrading session. Usage: startwork [@participants] [# of sessions] [task duration] [break duration]";
        }

        @Override
        public PermissionLevel getRequiredPermissionLevel() {
            return PermissionLevel.ANY;
        }

        @Override
        public void execute(final Message message, final DiscordApi discordApi) {
            final List<User> users = new ArrayList<>();
            users.addAll(message.getMentionedUsers());
            final User author = message.getAuthor().asUser().get();
            if (!users.contains(author)) {
                users.add(author);
            }
            final int sessionID = counter.getAndIncrement();
            final String[] args = message.getContent().split(" ");
            int sessionNumber = 1;
            int sessionDuration = 25;
            int sessionBreak = 5;
            int argCount = 0;
            for (int i = 0; i < args.length; i++) {
                try {
                    final int value = Integer.parseInt(args[i]);
                    switch (argCount) {
                        case 0:
                            sessionNumber = value;
                            break;
                        case 1:
                            sessionDuration = value;
                            break;
                        case 2:
                            sessionBreak = value;
                            break;
                    }
                    argCount++;
                } catch (final NumberFormatException e) {
                    // No-Op
                }
            }
            final Session createdSession = Session.builder()
                    .sessionID(sessionID)
                    .creator(author)
                    .members(users)
                    .sessionsRemaining(sessionNumber)
                    .sessionDuration(sessionDuration)
                    .sessionBreak(sessionBreak)
                    .isEnabled(true)
                    .timeElapsed(0)
                    .build();
            sessions.put(sessionID + "", createdSession);
            String members = "";
            for (final User user : users) {
                members += user.getName() + ", ";
            }
            members = members.substring(0, members.length() - 2);
            new MessageBuilder()
                    .setEmbed(new EmbedBuilder()
                            .setColor(Color.GREEN)
                            .setTitle("Session " + sessionID + " created")
                            .addField("Creator", author.getName())
                            .addField("Members", members)
                            .addField("Work Time", sessionDuration + " minute(s)")
                            .addField("Break Time", sessionBreak + " minute(s)"))
                    .send(message.getChannel());
            startSessionTimer(createdSession, message.getChannel());
        }
    }

    /**
     * Deletes a session.
     */
    final class EndWorkCommand implements Command {

        @Override
        public String getName() {
            return "endwork";
        }

        @Override
        public String getDescription() {
            return "Ends a tasktrading session if you are the creator. Usage: endwork";
        }

        @Override
        public PermissionLevel getRequiredPermissionLevel() {
            return PermissionLevel.ANY;
        }

        @Override
        public void execute(final Message message, final DiscordApi discordApi) {
            Session session = null;
            for (final Session sessionInMap : sessions.values()) {
                if (sessionInMap.getCreator() == message.getAuthor().asUser().get()) {
                    session = sessionInMap;
                }
            }
            if (session == null) {
                new MessageBuilder()
                        .setContent("You are not the creator of any sessions.")
                        .send(message.getChannel());
            } else {
                session.setEnabled(false);
                sessions.remove(session.getSessionID() + "");
                new MessageBuilder()
                        .setContent("Session " + session.getSessionID() + " terminated.")
                        .send(message.getChannel());
            }
        }
    }

    /**
     * Leaves a session.
     */
    final class LeaveWorkCommand implements Command {

        @Override
        public String getName() {
            return "leavework";
        }

        @Override
        public String getDescription() {
            return "Leaves a tasktrading session. Usage: leavework";
        }

        @Override
        public PermissionLevel getRequiredPermissionLevel() {
            return PermissionLevel.ANY;
        }

        @Override
        public void execute(final Message message, final DiscordApi discordApi) {
            Session session = null;
            final User author = message.getAuthor().asUser().get();
            for (final Session sessionInMap : sessions.values()) {
                if (sessionInMap.getMembers().contains(author)) {
                    session = sessionInMap;
                }
            }
            if (session == null) {
                new MessageBuilder()
                        .setContent("You are not in any sessions.")
                        .send(message.getChannel());
            } else {
                final List<User> members = session.getMembers() ;
                members.remove(author);
                new MessageBuilder()
                        .setContent("You've left session " + session.getSessionID() + ".")
                        .send(message.getChannel());
                if (members.size() == 0) {
                    sessions.remove(session.getSessionID());
                    session.setEnabled(false);
                    new MessageBuilder()
                            .setContent("Session " + session.getSessionID() + " terminated due to no members.")
                            .send(message.getChannel());
                }
            }
        }
    }

    final class ModifyWorkCommand implements Command {

        @Override
        public String getName() {
            return "modifywork";
        }

        @Override
        public String getDescription() {
            return "Modifies a tasktrading session. Usage: modifywork [new task duration] [new break duration]";
        }

        @Override
        public PermissionLevel getRequiredPermissionLevel() {
            return PermissionLevel.ANY;
        }

        @Override
        public void execute(final Message message, final DiscordApi discordApi) {
            final String[] args = message.getContent().split(" ");
            if (args.length != 3) {
                new MessageBuilder()
                        .setContent("Invalid arguments. Usage: modifywork [new task duration] [new break duration]")
                        .send(message.getChannel());
            } else {
                Session session = null;
                final User author = message.getAuthor().asUser().get();
                for (final Session sessionInMap : sessions.values()) {
                    if (sessionInMap.getCreator() == author) {
                        session = sessionInMap;
                    }
                }
                if (session == null) {
                    new MessageBuilder()
                            .setContent("You are not the creator of any sessions.")
                            .send(message.getChannel());
                } else {
                    try {
                        final int newDuration = Integer.parseInt(args[1]);
                        final int newBreak = Integer.parseInt(args[2]);
                        session.setSessionDuration(newDuration);
                        session.setSessionBreak(newBreak);
                        new MessageBuilder()
                                .setEmbed(new EmbedBuilder()
                                        .setColor(Color.GREEN)
                                        .setTitle("Session " + session.getSessionID() + " modified")
                                        .addField("Creator", session.getCreator().getName())
                                        .addField("New Work Time", session.getSessionDuration() + " minute(s)")
                                        .addField("New Break Time", session.getSessionBreak() + " minute(s)"))
                                .send(message.getChannel());
                    } catch (final NumberFormatException e) {
                        new MessageBuilder()
                                .setContent("Invalid arguments. [new task duration] and [new break duration] must be integers.")
                                .send(message.getChannel());
                    }
                }
            }
        }
    }

    final class DeleteWorkCommand implements Command {

        @Override
        public String getName() {
            return "deletework";
        }

        @Override
        public String getDescription() {
            return "Deletes a session. Must be a mod or admin to run this command. Usage: deletework [sessionID]";
        }

        @Override
        public PermissionLevel getRequiredPermissionLevel() {
            return PermissionLevel.MOD;
        }

        @Override
        public void execute(final Message message, final DiscordApi discordApi) {
            final String[] args = message.getContent().split(" ");
            if (args.length != 2) {
                new MessageBuilder()
                        .setContent("Invalid arguments. Usage: deletework [sessionID]")
                        .send(message.getChannel());
            } else {
                if (!sessions.containsKey(args[1])) {
                    new MessageBuilder()
                            .setContent("No session with that ID exists.")
                            .send(message.getChannel());
                } else {
                    final Session session = sessions.get(args[1]);
                    session.setEnabled(false);
                    sessions.remove(args[1]);
                    new MessageBuilder()
                            .setContent("Session " + session.getSessionID() + " terminated.")
                            .send(message.getChannel());
                }
            }
        }
    }

    final class JoinWorkCommand implements Command {

        @Override
        public String getName() {
            return "joinwork";
        }

        @Override
        public String getDescription() {
            return "Joins a tasktrading session. Usage: joinwork [sessionID]";
        }

        @Override
        public PermissionLevel getRequiredPermissionLevel() {
            return PermissionLevel.ANY;
        }

        @Override
        public void execute(final Message message, final DiscordApi discordApi) {
            final String[] args = message.getContent().split(" ");
            if (args.length != 2) {
                new MessageBuilder()
                        .setContent("Invalid arguments. Usage: deletework [sessionID]")
                        .send(message.getChannel());
            } else {
                if (!sessions.containsKey(args[1])) {
                    new MessageBuilder()
                            .setContent("No session with that ID exists.")
                            .send(message.getChannel());
                } else {
                    final Session session = sessions.get(args[1]);
                    final List<User> users = session.getMembers();
                    final User author = message.getAuthor().asUser().get();
                    if (users.contains(author)) {
                        new MessageBuilder()
                                .setContent("You've already joined session " + session.getSessionID() + ".")
                                .send(message.getChannel());
                    } else {
                        users.add(author);
                        new MessageBuilder()
                                .setContent("You've joined session " + session.getSessionID() + "!")
                                .send(message.getChannel());
                    }
                }
            }
        }
    }

    final class ListWorkCommand implements Command {

        @Override
        public String getName() {
            return "listwork";
        }

        @Override
        public String getDescription() {
            return "Lists all ongoing tasktrading sessions. Usage: listwork";
        }

        @Override
        public PermissionLevel getRequiredPermissionLevel() {
            return PermissionLevel.ANY;
        }

        @Override
        public void execute(final Message message, final DiscordApi discordApi) {
            if (sessions.size() == 0) {
                new MessageBuilder()
                        .setContent("There are no current sessions.");
            } else {
                sessions.values().forEach(session -> {
                    final List<User> users = session.getMembers();
                    String members = "";
                    for (final User user : users) {
                        members += user.getName() + ", ";
                    }
                    members = members.substring(0, members.length() - 2);
                    new MessageBuilder()
                            .setEmbed(new EmbedBuilder()
                                    .setTitle("Session " + session.getSessionID())
                                    .setColor(Color.CYAN)
                                    .addField("Creator", session.getCreator().getName())
                                    .addField("Members", members)
                                    .addField("Work Time", session.getSessionDuration() + " minute(s)")
                                    .addField("Break Time", session.getSessionBreak() + " minute(s)")
                                    .addField("Time Remaining In Current Session",
                                            session.getSessionDuration() - session.getTimeElapsed() + " minutes(s)")
                                    .addField("Sessions Remaining", session.getSessionsRemaining() + " session(s)")
                            )
                            .send(message.getChannel());
                });
            }
        }
    }
}
