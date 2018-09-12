package com.wbigelow.ancillary;

import com.google.common.collect.ImmutableList;
import com.google.common.reflect.ClassPath;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;

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
        final String token = args[0];
        final DiscordApi discordApi = new DiscordApiBuilder().setToken(token).login().join();
        discordApi.addMessageCreateListener(new MessageCreateListenerImpl(commandManager, discordApi));
        discordApi.addServerMemberJoinListener(new ServerMemberJoinListenerImpl());
    }
}
