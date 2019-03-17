package com.wbigelow.ancillary;

import com.vdurmont.emoji.EmojiParser;
import lombok.RequiredArgsConstructor;
import org.javacord.api.event.message.reaction.ReactionAddEvent;
import org.javacord.api.listener.message.reaction.ReactionAddListener;

@RequiredArgsConstructor
public class PinReactionListenerImpl implements ReactionAddListener {

    @Override
    public void onReactionAdd(final ReactionAddEvent event) {
        event.getReaction().ifPresent(reaction -> {
            if (!reaction.getMessage().isPrivate() && reaction.getEmoji().equalsEmoji(EmojiParser.parseToUnicode(":pushpin:"))
                && event.getChannel().getId() == 546260920104517661L)
                reaction.getMessage().pin();
        });
    }
}
