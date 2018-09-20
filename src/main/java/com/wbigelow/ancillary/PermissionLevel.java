package com.wbigelow.ancillary;

/**
 * Permission levels for commands.
 */
public enum PermissionLevel {
    ANY, // Any user can run the command.
    MOD, // Only users with the set mod role can run the command.
    ADMIN // Only users with the set admin role can run the command.
}
