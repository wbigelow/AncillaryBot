package com.wbigelow.ancillary;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum PermissionLevel {
    ANY (0),
    MOD (1),
    ADMIN (2);
    @Getter
    private final int level;
}
