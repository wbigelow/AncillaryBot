package com.wbigelow.ancillary;

import lombok.RequiredArgsConstructor;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

@RequiredArgsConstructor
public class MessageCreateListenerImpl implements MessageCreateListener {
    private final CommandManager commandManager;
    private final DiscordApi discordBotClient;

    @Override
    public void onMessageCreate(final MessageCreateEvent event) {
        final Message message = event.getMessage();
        final String content = message.getContent();
        if (content.startsWith(">")) {
            final String command = content.contains(" ") ? content.substring(1, content.indexOf(" ")) : content.substring(1);
            commandManager.executeCommand(command, message, discordBotClient);
        }
    }
}
