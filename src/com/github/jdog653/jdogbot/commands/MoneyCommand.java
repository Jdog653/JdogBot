package com.github.jdog653.jdogbot.commands;

import com.github.jdog653.jdogbot.GlobalFunctions;
import com.github.jdog653.jdogbot.LMMoney;
import org.pircbotx.Channel;
import org.pircbotx.User;
import org.pircbotx.hooks.events.MessageEvent;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by jordan on 3/31/16.
 */
public class MoneyCommand implements Command
{
    private String baseCommandName;
    private int[] money;
    public MoneyCommand(String base)
    {
        baseCommandName = base;

        money = new int[LMMoney.values().length];
    }

    /*
    * !money add <item> <amount>
    * !money set <item> <amount>
    * !money
    * !money remove <item> <amount>
    * !money <item>
    * !money value <item>
    * !money list
    * !money clear
    * */
    @Override
    public String executeCommand(MessageEvent event, ArrayList<String> params)
    {
        if(params.size() == 0)
        {
            return event.getChannel().getName().replace("#", "") + "'s money count is: $" + NumberFormat.getInstance().format(calculateMoneyTotal());
        }

        String s = params.remove(0).toLowerCase();

        for(LMMoney m : LMMoney.values())
        {
            if(s.equalsIgnoreCase(m.toString().toLowerCase()))
            {
                //!money <item>
                return event.getChannel().getName().replace('#', '\0') + " has collected " + money[LMMoney.valueOf(params.get(0).toLowerCase()).ordinal()];
            }
        }

        switch(s)
        {
            case "add":
                return moneyCommandAdd(event, params);
            case "set":
                return moneyCommandSet(event, params);
            case "remove":
                return moneyCommandRemove(event, params);
            case "value":
                return moneyCommandValue(event, params);
            case "list":
                return moneyCommandList(event, params);
            case "clear":
                return moneyCommandClear(event, params);
            default:
                return "[!money] Parameter " + s + " is not recognized. Perhaps you misspelled it?";
        }
    }

    @Override
    public String getBaseCommandName()
    {
        return baseCommandName;
    }

    /**
     * Adds the specified amount of <item> to the money list
     * @param event The MessageEvent dispatched
     * @param params An ArrayList<String> with the parameters in the list
     * @return
     */
    //!money add <item> <amount>
    private String moneyCommandAdd(MessageEvent event, ArrayList<String> params)
    {
        switch(params.size())
        {
            case 0:
                params.add("1");
                return executeCommand(event, params);
            case 1:
                return "[!money add] Not enough parameters provided. Usage: !money add <item> <amount>";
            case 2:
                String s = params.remove(0);
                LMMoney m;

                try
                {
                    m = LMMoney.getEnum(s);
                }
                catch(IllegalArgumentException e)
                {
                    return "[!money add] Parameter " + s + " is not a valid item. Valid items are: " + Arrays.toString(LMMoney.values());
                }

                int num;

                s = params.remove(0);
                try
                {
                    num = Integer.decode(s);
                }
                catch(NumberFormatException e)
                {
                    return "[!money add] Parameter" + s + " is not a valid number. Please input an integer";
                }

                if(num > 0)
                {
                    money[m.ordinal()] += num;
                    return m + " count has been updated from " + (money[m.ordinal()] - num) + " -> " + money[m.ordinal()];
                }

                return "[!money add] Number of " + m + " provided is less than 1. Please add at least one " + m;
            default:
                return "[!money add] Number of parameters given is too large. Usage: !money add <item> <amount>";
        }
    }

    //Usage: !money set <item> <amount>
    private String moneyCommandSet(MessageEvent event, ArrayList<String> params)
    {
        User sender = event.getUser();
        Channel channel = event.getChannel();
        if(!(GlobalFunctions.isMod(sender, channel) || GlobalFunctions.isChannelOwner(sender, channel)))
        {
            return "I'm sorry, @" + sender.getNick() + ", but only mods can set money";
        }
        switch(params.size())
        {
            case 0:
                return "[!money set] Not enough parameters provided. Usage: !money add <item> <amount>";
            case 1:
                return "[!money set] Not enough parameters provided. Usage: !money add <item> <amount>";
            case 2:
                String s = params.remove(0);
                LMMoney m;
                try
                {
                    m = LMMoney.getEnum(s);
                }
                catch(IllegalArgumentException e)
                {
                    return "[!money set] Parameter " + s + " is not a valid item. Valid items are: " + Arrays.toString(LMMoney.values());
                }

                int num;

                s = params.remove(0);
                try
                {
                    num = Integer.decode(s);
                }
                catch(NumberFormatException e)
                {
                    return "[!money set] Parameter" + s + " is not a valid number. Please input an integer";
                }

                if(num < 0)
                {
                    return "[!money set] Invalid number of items selected. Please provide a positive number";
                }

                money[m.ordinal()] = num;
                return m + " count has been updated to " + money[m.ordinal()];
            default:
                return "[!money set] Number of parameters given is too large. Usage: !money set <item> <amount>";
        }
    }

    private String moneyCommandRemove(MessageEvent event, ArrayList<String> params)
    {
        switch(params.size())
        {
            case 0:
                return "[!money remove] Not enough parameters provided. Usage: !money remove <item> <amount>";
            case 1:
                return "[!money remove] Not enough parameters provided. Usage: !money remove <item> <amount>";
            case 2:
                String s = params.remove(0);
                LMMoney m;
                try
                {
                    m = LMMoney.getEnum(s);
                }
                catch(IllegalArgumentException e)
                {
                    return "[!money remove] Parameter " + s + " is not a valid item. Valid items are: " + Arrays.toString(LMMoney.values());
                }

                int num;

                s = params.remove(0);
                try
                {
                    num = Integer.decode(s);
                }
                catch(NumberFormatException e)
                {
                    return "[!money remove] Parameter" + s + " is not a valid number. Please input an integer";
                }

                if(num <= money[m.ordinal()] && num > 0)
                {
                    money[m.ordinal()] -= num;
                    return m + " count has been updated from " + (money[m.ordinal()] - num) + " -> " + money[m.ordinal()];
                }
                else if(num > money[m.ordinal()])
                {
                    return "[!money remove] Number of " + m  + " provided is greater than the number collected. Please provide a number between 1 and " + money[m.ordinal()];
                }

                return "[!money remove] Number of " + m + " provided is less than 1. Please remove at least one " + m;
            default:
                return "[!money remove] Number of parameters given is too large. Usage: !money remove <item> <amount>";
        }
    }

    private String moneyCommandValue(MessageEvent event, ArrayList<String> params)
    {
        LMMoney m;
        switch(params.size())
        {
            case 0:
                return "[!money value] Too few parameters given. Usage: !money value <item>";
            case 1:
                try
                {
                    m = LMMoney.getEnum(params.get(0));
                }
                catch(IllegalArgumentException e)
                {
                    return "[!money value] Parameter " + params.get(0) + " is not a valid item. Valid items are: " + Arrays.toString(LMMoney.values());
                }
                return "[!money value] The value of one " + m + " is: $" + NumberFormat.getInstance().format(m.getValue());
            default:
                return "[!money value] Too many parameters given. Usage: !money value <item>";
        }
    }

    private String moneyCommandList(MessageEvent event, ArrayList<String> params)
    {
        if(calculateMoneyTotal() == 0)
        {
            return event.getChannel().getName().replace("#", "") + " hasn't collected any money yet!";
        }

        String s = event.getChannel().getName().replace("#", "") + " has currently collected: ";

        for(LMMoney m : LMMoney.values())
        {
            s += m.toString() + ": " + money[m.ordinal()] + ", ";
        }

        return s;
    }

    private String moneyCommandClear(MessageEvent event, ArrayList<String> params)
    {
        User sender = event.getUser();
        Channel channel = event.getChannel();

        switch(params.size())
        {
            case 0:
                if(GlobalFunctions.isMod(sender, channel) || GlobalFunctions.isChannelOwner(sender, channel))
                {
                    if(calculateMoneyTotal() == 0)
                    {
                        return "[!money clear] You cannot clear an empty money list. Please add money before clearing it";
                    }
                    else
                    {
                        for (LMMoney m : LMMoney.values())
                        {
                            money[m.ordinal()] = 0;
                        }

                        return "All money has been cleared";
                    }
                }

                return "I'm sorry, @" + sender.getNick() + " but only mods can clear the money";
            default:
                return "[!money clear] Too many parameters given. Usage: !money clear";
        }
    }

    private int calculateMoneyTotal()
    {
        int total = 0;

        for(LMMoney m : LMMoney.values())
        {
            total += m.getValue() * money[m.ordinal()];
        }

        return total;
    }
}
