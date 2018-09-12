package com.wbigelow.ancillary;

import java.util.List;

/**
 * Interface for a command module.
 */
public interface Module {
    /**
     * Commands this module includes and their descriptions.
     * @return A list of command words.
     */
    List<Command> getCommands();
}
