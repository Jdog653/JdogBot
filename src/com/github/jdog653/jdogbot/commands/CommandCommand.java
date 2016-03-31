package com.github.jdog653.jdogbot.commands;

import org.pircbotx.hooks.events.MessageEvent;

import java.util.ArrayList;

/**
 * Created by jordan on 3/26/16.
 */
public class CommandCommand implements Command
{
    private String baseCommandName;

    public CommandCommand(String base)
    {
        baseCommandName = base;
    }

    @Override
    public String executeCommand(MessageEvent event, ArrayList<String> params)
    {
        return "To see an up-to-date list of commands, go to: https://github.com/Jdog653/JdogBot/wiki/commands";
    }

    @Override
    public String getBaseCommandName()
    {
        return baseCommandName;
    }
}
