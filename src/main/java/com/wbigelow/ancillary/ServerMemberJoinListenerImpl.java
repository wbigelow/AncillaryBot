package com.wbigelow.ancillary;

import org.javacord.api.event.server.member.ServerMemberJoinEvent;
import org.javacord.api.listener.server.member.ServerMemberJoinListener;

public class ServerMemberJoinListenerImpl implements ServerMemberJoinListener {
    private static final long JUST_JOINED_ROLE_ID = 452308369961648128L;
    @Override
    public void onServerMemberJoin(final ServerMemberJoinEvent event) {
        event.getServer().getRoleById(JUST_JOINED_ROLE_ID).ifPresent(role -> event.getUser().addRole(role));
    }
}
