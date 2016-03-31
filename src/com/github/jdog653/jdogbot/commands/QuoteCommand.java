package com.github.jdog653.jdogbot.commands;

import com.github.jdog653.jdogbot.GlobalFunctions;
import org.pircbotx.Channel;
import org.pircbotx.User;
import org.pircbotx.hooks.events.MessageEvent;

import java.util.ArrayList;

/**
 * Created by jordan on 3/26/16.
 */
public class QuoteCommand implements Command
{
    private String baseCommandName, quoteFile;
    private ArrayList<String> quotes;
    public QuoteCommand(String base, String file)
    {
        quotes = new ArrayList<String>();
        baseCommandName = base;
        quoteFile = file;
        GlobalFunctions.fileInit(quoteFile, quotes);
    }
    
    public String getBaseCommandName()
    {
        return baseCommandName;    
    }
    
    @Override
    public String executeCommand(MessageEvent event, ArrayList<String> params)
    {
        String str;
        int x;
        User sender = event.getUser();
        Channel channel = event.getChannel();

        //Someone called !quote. Interpreted as !quote <random number>
        if(params.size() == 0)
        {
            str = quoteCommandGet(((int) (Math.random() * quotes.size())) + 1);
            return str.startsWith("[!quote]") ? str : "[Random Quote] " + str;
        }
        //Someone called !quote <number> or !quote count
        else if(params.size() == 1)
        {
            if(params.get(0).equalsIgnoreCase("count"))
            {
                return "There are currently " + quotes.size() + " quotes in the database.";
            }

            try
            {
                x = Integer.decode(params.get(0));
            }
            catch(NumberFormatException e)
            {
                return "[!quote] Unfortunately," + params.get(0) + " is not a valid integer. Please try again.";
            }

            return quoteCommandGet(x);
        }
        else
        {
            String s = params.remove(0);

            switch(s)
            {
                case "add":
                    if(GlobalFunctions.isMod(sender, channel) || GlobalFunctions.isChannelOwner(sender, channel))
                    {
                        return quoteCommandAdd(event, params);
                    }
                    return "[!quote] I'm sorry, @" + sender + ", but only mods can add quotes.";
                case "remove":
                    if (GlobalFunctions.isMod(sender, channel) || GlobalFunctions.isChannelOwner(sender, channel))
                    {
                        return quoteCommandRemove(event, params);
                    }
                    return "[!quote] I'm sorry, @" + sender + ", but only mods can remove quotes.";
                case "edit":
                    if(GlobalFunctions.isMod(sender, channel) || GlobalFunctions.isChannelOwner(sender, channel))
                    {
                        return quoteCommandEdit(event, params);
                    }
                    return "[!quote] I'm sorry, @" + sender + ", but only mods can edit quotes.";
                default:
                    return "[!quote] Parameter " + s + " is not recognized as a valid parameter. Perhaps you misspelled it?";
            }
        }
    }

    private String quoteCommandAdd(MessageEvent event, ArrayList<String> params)
    {
        switch(params.size())
        {
            case 0:
                return "[!quote add] No arguments are given. Please give a quote to add.";
            default:
                String s = "";

                for(int i = 0; i < params.size(); i++)
                {
                    s += params.get(i);
                    if(i < params.size() - 1)
                    {
                        s += " ";
                    }
                }

                quotes.add(s);
                GlobalFunctions.writeListToFile(quoteFile, quotes);
                return "Successfully added quote #" + quotes.size() + " - " + s + " to the list";
        }
    }

    private String quoteCommandRemove(MessageEvent event, ArrayList<String> params)
    {
        switch(params.size())
        {
            //Someone typed !quote remove <nothing>
            case 0:
                return "[!quote remove]: Too not enough arguments given. Usage: !quote remove <quote number>";
            //Someone typed !quote remove <quote number>
            case 1:
                try
                {
                    int x = Integer.decode(params.get(0));

                    if(x > quotes.size())
                    {
                        return "[!quote remove] Argument provided exceeds number of quotes in the file. Please try again. (" + x + "/" + quotes.size() + ")";
                    }
                    else if(x <= 0)
                    {
                        return "[!quote remove] Argument provided is less than or equal to zero. Please try again. (" + x + "/" + quotes.size() + ")";
                    }

                    quotes.remove(x - 1);

                    GlobalFunctions.writeListToFile(quoteFile, quotes);
                    return "Quote " + x + " successfully removed from the quote list";
                }
                catch(NumberFormatException e)
                {
                    return "[!quote remove] Argument provided is not an integer. Please try again.";
                }
            default:
                //Someone typed !quote set <something> <something> ... <more something>
                return "[!quote remove]: Too many arguments given. Usage: !quote remove <quote number>";
        }


    }

    private String quoteCommandEdit(MessageEvent event, ArrayList<String> params)
    {
        switch(params.size())
        {
            case 0:
            case 1:
                return "[!quote edit] Invalid number of parameters given. Usage: !quote edit <quote number> <revised quote>";
            default:
                try
                {
                    String s = params.remove(0);
                    int x = Integer.parseInt(s);

                    if(x > 0 && x <= quotes.size())
                    {
                        s = "";

                        for (int i = 0; i < params.size(); i++)
                        {
                            s += params.get(i);
                            if (i < params.size() - 1)
                            {
                                s += " ";
                            }
                        }

                        quotes.set(x - 1, s);
                        GlobalFunctions.writeListToFile(quoteFile, quotes);
                        return "Quote #" + x + " Successfully set to: " + s;
                    }
                    return "[!quote edit] Parameter given must be in the range: [1, " + quotes.size() + "]";
                }
                catch(NumberFormatException e)
                {
                    return "[!quote edit] Argument provided exceeds number of quotes in the file. Please try again. (\" + x + \"/\" + quotes.size() + \")\"";
                }
        }
    }

    private String quoteCommandGet(int x)
    {
        //Special case - empty quote list
        if(quotes.size() == 0)
        {
            return "[!quote] There are not any quotes currently in the list. Have a mod add some quotes and then try again";
        }

        if(x > quotes.size())
        {
            return "[!quote] Argument provided exceeds number of quotes in the file. Please try again. (" + x + "/" + quotes.size() + ")";
        }
        else if(x <= 0)
        {
            return "[!quote] Argument provided is less than or equal to zero. Please try again. (" + x + "/" + quotes.size() + ")";
        }

        return "Quote " + x + "/" + quotes.size() + ": " + quotes.get(x - 1);
    }
}
