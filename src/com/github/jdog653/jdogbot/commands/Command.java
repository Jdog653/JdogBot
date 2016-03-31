package com.github.jdog653.jdogbot.commands;

import org.pircbotx.hooks.events.MessageEvent;

import java.util.ArrayList;

/**
 * Created by jordan on 3/26/16.
 */
public interface Command
{
    enum CurrentCommands
    {
        QUOTE, HELP, COMMAND, TIMER, MONEY
    }

    String executeCommand(MessageEvent event, ArrayList<String> params);
    String getBaseCommandName();
}
