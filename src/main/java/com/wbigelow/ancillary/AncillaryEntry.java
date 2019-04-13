package com.wbigelow.ancillary;

import com.google.common.collect.ImmutableList;
import com.google.common.reflect.ClassPath;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.activity.ActivityType;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Scanner;
import java.util.Set;

/**
 * Entry point for Ancillary. Fetches all modules and connects to the discord server.
 */
public class AncillaryEntry {
    private static final String MODULES_PACKAGE = "com.wbigelow.ancillary.modules";
    private static CommandManager commandManager;

    public static void main(final String[] args) {
        final ImmutableList.Builder<Module> commandListBuilder = ImmutableList.builder();
        try {
            final ClassPath classPath = ClassPath.from(Thread.currentThread().getContextClassLoader());
            final Set<ClassPath.ClassInfo> classes = classPath.getTopLevelClasses(MODULES_PACKAGE);
            classes.forEach(classInfo -> {
                try {
                    commandListBuilder.add((Module) classInfo.load().getConstructor().newInstance());
                } catch (final NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            });
        } catch (final IOException e) {
            e.printStackTrace();
        }
        commandManager = new CommandManager(commandListBuilder.build());
        final String token;
        if (System.getenv("DISCORD_TOKEN") != null) {
            // Fetch the token from the environ over the file if the environ is set.
            token = System.getenv("DISCORD_TOKEN");
        } else {
            try {
                token = new Scanner(new File("token.txt")).nextLine();
            } catch (final FileNotFoundException e) {
                e.printStackTrace();
                return;
            }
        }
        final DiscordApi discordApi = new DiscordApiBuilder().setToken(token).login().join();
        discordApi.addMessageCreateListener(new MessageCreateListenerImpl(commandManager, discordApi));
        discordApi.updateActivity(ActivityType.PLAYING, ">help");
        //discordApi.addServerMemberJoinListener(new ServerMemberJoinListenerImpl());
        //discordApi.addMessageDeleteListener(new MessageDeleteListenerImpl());
        discordApi.addReactionAddListener(new PinReactionListenerImpl());
        System.out.println(discordApi.createBotInvite());
    }
}
