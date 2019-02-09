package com.wbigelow.ancillary;

import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageDeleteEvent;
import org.javacord.api.listener.message.MessageDeleteListener;

import java.util.Optional;

public class MessageDeleteListenerImpl implements MessageDeleteListener {

    @Override
    public void onMessageDelete(final MessageDeleteEvent event) {
        final ServerTextChannel channel = event.getApi().getServerTextChannelsByName("ancillary-logging").iterator().next();
        final EmbedBuilder builder = new EmbedBuilder()
                .setTitle("Message Deleted");
        final Optional<Message> message = event.getMessage();
        message.ifPresent(m ->
                builder.setAuthor(m.getAuthor()));
        if (!message.isPresent())
            builder.setAuthor("Unknown Author");
        event.getServerTextChannel().ifPresent(c ->
                builder.addField("Channel", c.getName()));
        event.getMessage().ifPresent(m ->
                builder.addField("Message", m.getContent()));
        new MessageBuilder()
                .setEmbed(builder)
                .send(channel);
    }
}
