package com.github.jdog653.jdogbot.commands;

import com.github.jdog653.jdogbot.GlobalFunctions;
import org.pircbotx.hooks.events.MessageEvent;

import java.util.ArrayList;

/**
 * Created by jordan on 3/26/16.
 */
public class HelpCommand implements Command
{
    private String baseCommandName;
    public HelpCommand(String base)
    {
        baseCommandName = base;
    }
    @Override
    public String executeCommand(MessageEvent event, ArrayList<String> params)
    {
        switch(params.size())
        {
            case 0:
                return "To read about JdogBot, go to: https://github.com/Jdog653/JdogBot/wiki";
            case 1:
                break;
            default:
                return "[!help] Too many parameters given. Usage: !help  <command>";
        }
        String s = params.get(0);

        if(s.startsWith("!"))
        {
            s = s.substring(1);
        }

        if(GlobalFunctions.isCommand(s))
        {
            return "The Wiki page for " + s + " can be found at: https://github.com/Jdog653/JdogBot/wiki/" + s;
        }

        return "[!help] I'm sorry, @" + event.getUser().getNick() + ", but " + s + " is not a valid command.";
    }

    @Override
    public String getBaseCommandName()
    {
        return baseCommandName;
    }
}
