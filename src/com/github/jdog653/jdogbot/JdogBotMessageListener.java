package com.github.jdog653.jdogbot;

import com.github.jdog653.jdogbot.commands.*;
import org.pircbotx.Channel;
import org.pircbotx.User;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by jordan on 3/26/16.
 */
public class JdogBotMessageListener extends ListenerAdapter
{
    //private final String QUOTES_FILENAME = "Files\\Quote List.txt";
    private final String QUOTES_FILENAME = "Files/Quote List.txt";
    private final int MAX_TIMERS = 5;
    private final Command[] COMMANDS = {new QuoteCommand("quote", QUOTES_FILENAME), new HelpCommand("help"),
                                        new CommandCommand("command"), new TimerCommand("timer", MAX_TIMERS),
                                        new MoneyCommand("money")};

    @Override
    public void onMessage(MessageEvent event)
    {
        String s, response = "";
        User sender = event.getUser();
        Channel channel = event.getChannel();

        //Split message into words using a blank space as a delimiter
        ArrayList<String> params = new ArrayList<>(Arrays.asList(event.getMessage().split(" ")));
        s = params.remove(0);

        //It's a command
        if(s.startsWith("!"))
        {
            //Remove the ! from the command
            s = s.substring(1, s.length());

            for(Command c : COMMANDS)
            {
                if(s.equals(c.getBaseCommandName()))
                {
                    response = c.executeCommand(event, params);
                    break;
                }
            }
        }

        //If there's an actual message to be sent
        if(!response.equalsIgnoreCase(""))
        {
            event.getChannel().send().message(response);
        }
    }
}
