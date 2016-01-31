import org.apache.commons.lang3.time.DurationFormatUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.pircbotx.Channel;
import org.pircbotx.User;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.JoinEvent;
import org.pircbotx.hooks.events.MessageEvent;

import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class MessageListener extends ListenerAdapter
{
    private final int RAID_ITERATIONS = 10, MAX_TIMERS = 5, HOURS_TO_MINUTES = 60, MINUTES_TO_SECONDS = 60, SECONDS_TO_MILLISECONDS = 1000;
    private boolean capSent, giveawayActive;
    private final ArrayList<String> MODS = new ArrayList<>(Arrays.asList("jdog653","theofficialskozzy", "jdogbot", "mollyranchers"));
    private final String QUOTES_FILENAME = "Quote List.txt", GENERIC_RAID_MESSAGE = "Jdog Raid!";
    private Map<String, String> giveawayWinners;
    private ArrayList<String> quotes, giveawayEntrants;
    private int[] money;
    private Timer[] timers;
    private String agarioServer, giveawayItem, giveawayPhrase;

    public MessageListener()
    {
        timers = new Timer[MAX_TIMERS];

        for(int i = 0; i < MAX_TIMERS; i++)
        {
            timers[i] = null;
        }

        agarioServer = "";
        giveawayEntrants = new ArrayList<>();
        giveawayWinners = new HashMap<>();
        quotes = new ArrayList<>();
        capSent = false;
        giveawayActive = false;
        money = new int[LMMoney.values().length];
        fileInit(QUOTES_FILENAME, quotes);
    }

    public class TimerRolloverListener implements ActionListener
    {
        private int index;
        private Channel channel;
        private long time;

        /**
         *
         * @param i The index of the Timer[] that this listener is attached to
         * @param c The Channel from which this timer was started
         * @param t The time in Milliseconds after Jan 1 1970 that the timer was started
         */
        TimerRolloverListener(int i, Channel c, long t)
        {
            index = i;
            channel = c;
            time = t;
        }

        public long getTime()
        {
            return time;
        }

        public Channel getChannel()
        {
            return channel;
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            timers[index].stop();
            timers[index] = null;
            channel.send().message("Timer " + (index + 1) + " is up!");
        }
    }

    private void writeListToFile(final String fileName, final ArrayList<String> list)
    {
        try
        {
            FileWriter writer = new FileWriter(new File(fileName));

            for(int i = 0; i < list.size(); i++)
            {
                if(i < list.size() - 1)
                {
                    writer.write(list.get(i) + "\n");
                    writer.flush();
                }
                else
                {
                    writer.write(list.get(i));
                    writer.flush();
                }
            }
            writer.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    private void fileInit(final String fileName, ArrayList<String> list)
    {
        if(Files.exists(Paths.get(fileName)))
        {

            try
            {
                Scanner reader = new Scanner(new File(fileName));

                while (reader.hasNextLine())
                {
                    list.add(reader.nextLine());
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
    
    private void writeQuotesToFile()
    {
        try
        {
            FileWriter writer = new FileWriter(new File(QUOTES_FILENAME));

            for(int i = 0; i < quotes.size(); i++)
            {
                if(i < quotes.size() - 1)
                {
                    writer.write(quotes.get(i) + "\n");
                    writer.flush();
                }
                else
                {
                    writer.write(quotes.get(i));
                    writer.flush();
                }
            }
            writer.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }
    private boolean isChannelOwner(User sender, Channel channel)
    {
        //Does the username equal channel name without the #?
        return sender.getNick().equalsIgnoreCase(channel.getName().replace("#", ""));
        //return MODS.contains(sender.getNick().toLowerCase());
    }

    private void quoteFileInit()
    {
        if(Files.exists(Paths.get(QUOTES_FILENAME)))
        {
            try
            {
                Scanner reader = new Scanner(new File(QUOTES_FILENAME));

                while(reader.hasNextLine())
                {
                    quotes.add(reader.nextLine());
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    /*private String quoteCommandAdd(MessageEvent event, ArrayList<String> params)
    {
        User sender = event.getUser();
        Channel channel = event.getChannel();
        if(isMod(sender, channel))
        {
            switch (params.size())
            {
                case 0:
                    return "[!quote add] No arguments are given. Please give a quote to add.";
                default:
                    String s = "";

                    for (int i = 0; i < params.size(); i++)
                    {
                        s += params.get(i);
                        if (i < params.size() - 1)
                        {
                            s += " ";
                        }
                    }

                    quotes.add(s);
                    writeListToFile(QUOTES_FILENAME, quotes);
                    return "Successfully added quote #" + quotes.size() + " - " + s + " to the list";
            }
        }
        return "[!quote add] I'm sorry, @" + sender.getNick() + ", but only mods can add quotes.";
    }

    private String quoteCommandRemove(MessageEvent event, ArrayList<String> params)
    {
        User sender = event.getUser();
        Channel channel = event.getChannel();

        if(isMod(sender, channel))
        {
            switch (params.size())
            {
                //Someone typed !quote remove <nothing>
                case 0:
                    return "[!quote remove]: Not enough arguments given. Usage: !quote remove <quote number>";
                //Someone typed !quote remove <quote number>
                case 1:
                    try
                    {
                        int x = Integer.decode(params.get(0));

                        if (x > quotes.size())
                        {
                            return "[!quote remove] Argument provided exceeds number of quotes in the file. Please try again. (" + x + "/" + quotes.size() + ")";
                        }
                        else if (x <= 0)
                        {
                            return "[!quote remove] Argument provided is less than or equal to zero. Please try again. (" + x + "/" + quotes.size() + ")";
                        }

                        quotes.remove(x - 1);

                        writeListToFile(QUOTES_FILENAME, quotes);
                        return "Quote " + x + " successfully removed from the quote list";
                    }
                    catch (NumberFormatException e)
                    {
                        return "[!quote remove] Argument provided is not an integer. Please try again.";
                    }
                default:
                    //Someone typed !quote set <something> <something> ... <more something>
                    return "[!quote remove]: Too many arguments given. Usage: !quote remove <quote number>";
            }
        }

        return "[!quote remove] I'm sorry, @" + sender.getNick() + ", but only mods can remove quotes.";
    }

    private String quoteCommand(MessageEvent event, ArrayList<String> params)
    {
        int x;
        User sender = event.getUser();
        Channel channel = event.getChannel();

        //Someone called !quote. Interpreted as !quote <random number>
        if(params.size() == 0)
        {
            return "[Random Quote] " + quoteCommandGet(((int) (Math.random() * quotes.size())) + 1);
        }
        //Someone called !quote <number> or !quote count
        else if(params.size() == 1)
        {
            if(params.get(0).equalsIgnoreCase("count"))
            {
                return "There are " + quotes.size() + " quotes currently in the database.";
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
                    return quoteCommandAdd(event, params);
                case "remove":
                    return quoteCommandRemove(event, params);
                 default:
                    return "[!quote] Parameter " + s + " is not recognized as a valid parameter. Perhaps you misspelled it?";
            }
        }
    }

    private String quoteCommandGet(int x)
    {
        if(x > quotes.size())
        {
            return "[!quote] Argument provided exceeds number of quotes in the file. Please try again. (" + x + "/" + quotes.size() + ")";
        }
        else if(x <= 0)
        {
            return "[!quote] Argument provided is less than or equal to zero. Please try again. (" + x + "/" + quotes.size() + ")";
        }

        return "Quote " + x + "/" + quotes.size() + ": " + quotes.get(x - 1);
    }*/
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
                writeListToFile(QUOTES_FILENAME, quotes);
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

                    writeListToFile(QUOTES_FILENAME, quotes);
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
                        writeListToFile(QUOTES_FILENAME, quotes);
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

    private String quoteCommand(MessageEvent event, ArrayList<String> params)
    {
        int x;
        User sender = event.getUser();
        Channel channel = event.getChannel();

        //Someone called !quote. Interpreted as !quote <random number>
        if(params.size() == 0)
        {
            return "[Random Quote] " + quoteCommandGet(((int) (Math.random() * quotes.size())) + 1);
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
                    if(isMod(sender, channel) || isChannelOwner(sender, channel))
                    {
                        return quoteCommandAdd(event, params);
                    }
                    return "[!quote] I'm sorry, @" + sender + ", but only mods can add quotes.";
                case "remove":
                    if (isMod(sender, channel) || isChannelOwner(sender, channel))
                    {
                        return quoteCommandRemove(event, params);
                    }
                    return "[!quote] I'm sorry, @" + sender + ", but only mods can remove quotes.";
                case "edit":
                    if(isMod(sender, channel) || isChannelOwner(sender, channel))
                    {
                        return quoteCommandEdit(event, params);
                    }
                    return "[!quote] I'm sorry, @" + sender + ", but only mods can edit quotes.";
                default:
                    return "[!quote] Parameter " + s + " is not recognized as a valid parameter. Perhaps you misspelled it?";
            }
        }
    }

    private String quoteCommandGet(int x)
    {
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

    private boolean isMod(User sender, Channel channel)
    {
        return channel.getOps().contains(sender) || MODS.contains(sender.getNick().toLowerCase());
        //return channel.getOps().contains(sender);
    }

    private String raidCommand(MessageEvent event, ArrayList<String> params)
    {
        User sender = event.getUser();
        Channel channel = event.getChannel();

        if(isMod(sender, channel))
        {
            String target = "www.twitch.tv/", message = "Raid Message: ";
            int repeat;

            switch(params.size())
            {
                //They just typed !raid
                case 0:
                    return "[!raid] insufficient number of parameters given. Usage: !raid <target> <message> | <repeat>";
                //They typed !raid <user> - gives a generic message and default 10 times
                case 1:
                    params.add(GENERIC_RAID_MESSAGE);
                    params.add("" + RAID_ITERATIONS);
                    return raidCommand(event, params);
                //They typed !raid <user> <message> maybe <repeat>
                default:
                    //See if the last entry is an integer
                    try
                    {
                        repeat = Integer.decode(params.get(params.size() - 1));
                        params.remove(params.size() - 1);
                    }
                    catch(NumberFormatException e)
                    {
                        //If it's not an integer, default to the constant
                        repeat = RAID_ITERATIONS;
                    }
                    target += params.remove(0);

                    for(int i = 0; i < params.size(); i++)
                    {
                        if(i < params.size() - 1)
                        {
                            message += params.get(i) + " ";
                        }
                        //Last entry in the list. Don't add a space after it
                        else
                        {
                            message += params.get(i);
                        }
                    }

                    for(int i = 0; i < repeat; i++)
                    {
                        event.getChannel().send().message(target + " " + message);
                    }

                    //We've sent all the messages we need. Return a blank string
                    return "";
            }
        }
        return "I'm sorry, @" + event.getUser().getNick() + ", but only mods can use the !raid command";
    }

    public void onJoin(JoinEvent event)
    {
        if(!capSent)
        {
            capSent = true;
            event.getBot().sendCAP().request("twitch.tv/membership");
            event.getBot().sendCAP().request("twitch.tv/commands");
        }
    }
    public String serverCommandSet(MessageEvent event, ArrayList<String> params)
    {
        User sender = event.getUser();
        Channel channel = event.getChannel();

        if(isMod(sender, channel))
        {
            switch (params.size())
            {
                case 0:
                    return "[!server set] No arguments given. Usage: !server set <server>";
                default:
                    agarioServer = "";

                    for (int i = 0; i < params.size(); i++)
                    {
                        agarioServer += params.get(i);
                        if (i != params.size() - 1)
                        {
                            agarioServer += " ";
                        }
                    }
                    return "Server successfully set to: " + agarioServer;
            }
        }
        return "I'm sorry, " + sender.getNick() + ", but only mods can set the current server";
    }

    public String serverCommand(MessageEvent event, ArrayList<String> params)
    {
        String s;
        switch(params.size())
        {
            //!server -> returns current Agar.io Server
            case 0:
                if(agarioServer.equalsIgnoreCase(""))
                {
                    return "[!server] No Server is currently saved. Please ask a mod to set the current server";
                }
                return "The current server is: " + agarioServer;
            //!server <something> ... <something else>
            default:
                s = params.remove(0);
                switch(s)
                {
                    case "set":
                        return serverCommandSet(event, params);
                    default:
                        return "[!server] Parameter " + params.get(0) + " is not recognized as a valid parameter. Perhaps you misspelled it?";
                }
        }
    }

    private String slapCommand(MessageEvent event, ArrayList<String> params)
    {
        User sender = event.getUser();
        Channel channel = event.getChannel();

        if(isMod(sender, channel))
        {
            switch (params.size())
            {
                case 0:
                    return "[!slap] Insufficient number of parameters given. Usage: !slap <user>";
                case 1:
                    return ".me slaps " + params.get(0) + " around a bit with a large trout";
                default:
                    return "[!slap] Too many parameters given. Given: " + params.size() + " Required: 1. Usage: !slap <user>";
            }
        }

        return "I'm sorry, @" + sender.getNick() + ", but only mods can slap people";
    }

    //Usage: !pb <game> <category> | <runner>
    private String pbCommand(MessageEvent event, ArrayList<String> params)
    {
        return "";
    }

    private String giveawayCommand(MessageEvent event, ArrayList<String> params)
    {
        String s;
        switch(params.size())
        {
            //!giveaway - interpreted as !giveaway status
            case 0:
                params.add("status");
                return giveawayCommand(event, params);
            //!giveaway start, stop, or status
            case 1:
                s = params.remove(0);
                switch(s)
                {
                    case "choose":
                        return giveawayCommandChoose(event, params);
                    case "start":
                        if(!giveawayPhrase.equalsIgnoreCase("") && !giveawayItem.equalsIgnoreCase(""))
                        {
                            giveawayActive = true;
                            return "Giveaway for " + giveawayItem + " is now active. Say \"" + giveawayPhrase + "\" in chat to be considered";
                        }
                        return "[!giveaway start] A phrase and/or item has not been set. Please set them before starting";
                    case "stop":
                        int x = giveawayEntrants.size();
                        giveawayActive = false;
                        return "Giveaway for " + giveawayItem + " is now closed. There " + (x > 1 ? "are " + x + " entrants" : x == 1 ? "is a single entrant" : "aren't any entrants BibleThump");
                    case "status":
                        return "Giveaways Status: " + (giveawayActive ? "active" : "inactive") + " | Item: " + giveawayItem + " | Phrase: " + giveawayPhrase;
                    default:
                        return "[!giveaway] Parameter " + s + " is not recognized. Perhaps you misspelled it?";
                }
            //!giveaway set
            default:
                s = params.remove(0);
                switch(s)
                {
                    case "set":
                        return giveawayCommandSet(event, params);
                    default:
                        return "[!giveaway] Parameter " + s + " is not recognized as a valid parameter. Perhaps you misspelled it?";
                }

        }
    }

    private String giveawayCommandSet(MessageEvent event, ArrayList<String> params)
    {
        String s, str = "";
        if(params.size() > 0)
        {
            s = params.remove(0);

            for(int i = 0; i < params.size(); i++)
            {
                str += params.get(i);
                if(i < params.size() - 1)
                {
                    str += " ";
                }
            }

            switch (s)
            {
                case "item":
                    giveawayItem = str;
                    return "Giveaway item set to: " + str;
                case "phrase":
                    giveawayPhrase = str;
                    return "Giveaway phrase set to: " + str;
                default:
                    return "[!giveaway set] Parameter " + s + " is not valid. Valid parameters: item, phrase";
            }
        }
        return "[!giveaway set] No parameter given. Valid parameters: item, phrase";
    }

    private String giveawayCommandChoose(MessageEvent event, ArrayList<String> params)
    {
        if(!giveawayActive)
        {
            if(giveawayEntrants.size() > 0)
            {
                String winner = giveawayEntrants.get((int)((giveawayEntrants.size()) * Math.random()));
                giveawayEntrants.clear();
                giveawayWinners.put(winner, giveawayItem);

                return "Congratulations, @" + winner + "! You just won " + giveawayItem + "!";
            }
            return "There were no entrants to the giveaway. Please start the giveaway again so that someone may enter";
        }

        return "The giveaway is still active! Have a Mod use !giveaway stop before choosing a winner!";
    }

    private String shoutoutCommand(MessageEvent event, ArrayList<String> params)
    {
        if(isMod(event.getUser(), event.getChannel()))
        {
            switch(params.size())
            {
                case 0:
                    return "[!shoutout] No parameters given. Usage: !shoutout <username>";
                case 1:
                    return "Everyone should follow @" + params.get(0) + "! They're really kickass and totally deserve a follow :D twitch.tv/" + params.get(0);
                default:
                    return "[!shoutout] Too many parameters given. Usage: !shoutout <username>";
            }
        }
        return "I'm sorry, @" + event.getUser().getNick() + "; but only mods can use the !shoutout command";
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
                return moneyCommand(event, params);
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
        if(!(isMod(sender, channel) || isChannelOwner(sender, channel)))
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
                if(isMod(sender, channel) || isChannelOwner(sender, channel))
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
    private String moneyCommand(MessageEvent event, ArrayList<String> params)
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

    //!timer set <HH:mm:ss>
    //!timer start <timer number>
    //!timer status <timer number>
    //!timer stop <timer number>
    //!timer clear <timer number>
    //!timer list
    private String timerCommand(MessageEvent event, ArrayList<String> params)
    {
        switch(params.size())
        {
            case 0:
                return "";
            case 2:
                switch(params.get(0))
                {
                    case "set":
                        params.remove(0);
                        return timerCommandSet(event, params);
                    case "start":
                        params.remove(0);
                        return timerCommandStart(event, params);
                    case "stop":
                        params.remove(0);
                        return timerCommandStop(event, params);
                    case "status":
                        params.remove(0);
                        return timerCommandStatus(event, params);
                    case "clear":
                        params.remove(0);
                        return timerCommandClear(event, params);
                    default:
                        return "";

                }
            case 1:
                switch(params.get(0))
                {
                    case "list":
                        return timerCommandList(event, params);
                    default:
                        return "";
                }
            default:
                return "";

        }
    }

    private String timerCommandStart(MessageEvent event, ArrayList<String> params)
    {
        User sender = event.getUser();
        Channel channel = event.getChannel();
        int timer, timerIndex;

        switch(params.size())
        {
            case 0:
                return "[!timer start] Too few parameters provided. Usage: !timer start <timer number>";
            case 1:
                break;
            default:
                return "[!timer start] Too many parameters provided. Usage: !timer start <timer number>";
        }

        if(isMod(sender, channel))
        {
            try
            {
                timer = Integer.parseInt(params.get(0));

                timerIndex = timer - 1;
                if(timerIndex < 0)
                {
                    return "[!timer start] Invalid timer number. Parameter must be positive.";
                }
                else if(timerIndex >= MAX_TIMERS)
                {
                    return "[!timer start] Invalid timer number. Parameter must be less than or equal to " + MAX_TIMERS;
                }

                if(timers[timerIndex].getActionListeners().length == 0)
                {
                    if(!timers[timerIndex].isRunning())
                    {
                        timers[timerIndex] = new javax.swing.Timer(timers[timerIndex].getDelay(),
                                new TimerRolloverListener(timerIndex, channel, System.currentTimeMillis()));
                        timers[timerIndex].start();
                        return "Timer " + timer + " successfully started";
                    }
                    return "[!timer start] Timer " + timer + " is already running. You can't start it again.";
                }
                return "[!timer start] Timer " + timer + " has not been set yet, therefore you can't start it. " +
                        "Use !timer set <HH:mm:ss> to set a timer";

            }
            catch(NumberFormatException e)
            {
                return "[!timer start] " + params.get(0) + " is not a valid number. Please enter a positive integer";
            }
        }

        return "[!timer start] I'm sorry, @" + sender.getNick() + ", but only mods can start timers";
    }

    private String timerCommandStop(MessageEvent event, ArrayList<String> params)
    {
        User sender = event.getUser();
        Channel channel = event.getChannel();
        int timer, timerIndex;

        switch(params.size())
        {
            case 0:
                return "[!timer stop] Too few parameters provided. Usage: !timer stop <timer number>";
            case 1:
                break;
            default:
                return "[!timer stop] Too many parameters provided. Usage: !timer stop <timer number>";
        }

        if(isMod(sender, channel))
        {
            try
            {
                timer = Integer.parseInt(params.get(0));

                timerIndex = timer - 1;
                if(timerIndex < 0)
                {
                    return "[!timer stop] Invalid timer number. Number must be positive";
                }
                else if(timerIndex >= MAX_TIMERS)
                {
                    return "[!timer stop] Invalid timer number. Number must be less than or equal to " + MAX_TIMERS;
                }

                if(timers[timerIndex] != null && timers[timerIndex].isRunning())
                {
                    timers[timerIndex].stop();
                    return "Timer " + timer + " successfully stopped";

                }
                return "[!timer stop] You can't stop a timer that hasn't been started yet. Use !timer start " + timer + " to start it.";

            }
            catch(NumberFormatException e)
            {
                return "[!timer stop] " + params.get(0) + " is not a valid timer. Please enter a positive integer";
            }
        }

        return "[!timer stop] I'm sorry, @" + sender.getNick() + ", but only mods can stop timers";
    }

    private String timerCommandStatus(MessageEvent event, ArrayList<String> params)
    {
        User sender = event.getUser();
        Channel channel = event.getChannel();
        int timer, timerIndex;
        long duration;

        switch(params.size())
        {
            case 0:
                return "[!timer status] Too few parameters provided. Usage: !timer status <timer number>";
            case 1:
                break;
            default:
                return "[!timer status] Too many parameters provided. Usage: !timer statust <timer number>";
        }

        try
        {
            timer = Integer.parseInt(params.get(0));

            timerIndex = timer - 1;
            if(timerIndex < 0)
            {
                return "[!timer status] Timer number is invalid. Parameter must be positive";
            }
            else if(timerIndex >= MAX_TIMERS)
            {
                return "[!timer status] Timer number is invalid. Parameter must be less than or equal to " + MAX_TIMERS;
            }

            String response = " is not running.";
            TimerRolloverListener listener;
            if(timers[timerIndex] != null)
            {
                if(timers[timerIndex].isRunning())
                {
                    listener = ((TimerRolloverListener) (timers[timerIndex].getActionListeners()[0]));
                    duration = System.currentTimeMillis() - listener.getTime();
                    response = " has been running for [" + DurationFormatUtils.formatDuration(duration, "HH:mm:ss") + "]/["
                            + DurationFormatUtils.formatDuration(timers[timerIndex].getDelay(), "HH:mm:ss") +
                            "] On Channel: " + listener.getChannel().getName();
                }
                else
                {
                    response = " has been set to run for [" +
                            DurationFormatUtils.formatDuration(timers[timerIndex].getDelay(), "HH:mm:ss") + "]";
                }

            }

            return "Timer " + timer + response;
        }
        catch(NumberFormatException e)
        {
            return "[!timer status] " + params.get(0) + " is an invalid timer number. Parameter must be a positive integer";
        }
    }

    private String timerCommandClear(MessageEvent event, ArrayList<String> params)
    {
        User sender = event.getUser();
        Channel channel = event.getChannel();
        int timer, timerIndex;

        switch(params.size())
        {
            case 0:
                return "[!timer clear] Too few parameters provided. Usage: !timer clear <timer number>";
            case 1:
                break;
            default:
                return "[!timer clar] Too many parameters provided. Usage: !timer clear <timer number>";
        }

        if(isMod(sender, channel))
        {
            try
            {
                timer = Integer.parseInt(params.get(0));

                timerIndex = timer - 1;
                if(timerIndex < 0)
                {
                    return "[!timer clear] Invalid timer number. Timer must be positive";
                }
                else if(timerIndex >= MAX_TIMERS)
                {
                    return "[!timer clear] Invalid timer number. Timer must be less than or equal to " + MAX_TIMERS;
                }

                if(timers[timerIndex] != null)
                {
                    timers[timerIndex].stop();
                    timers[timerIndex] = null;
                    return "Timer " + timer + " has been cleared successfully";
                }

                return "[!timer clear] Timer " + timer + " has not been set; therefore, it can't be cleared";
            }
            catch(NumberFormatException e)
            {
                return "[!timer clear] " + params.get(0) + " is an invalid number. Number must be a positive integer";
            }
        }

        return "[!timer clear] I'm sorry, @" + sender.getNick() + ", but only mods can clear a timer";
    }

    private String timerCommandList(MessageEvent event, ArrayList<String> params)
    {
        //TODO: Change set time so it reflects when timer was started instead of when it was set
        //TODO: Make it so that a timer can only be started by a mod in the channel it was set
        String str = "";
        TimerRolloverListener listener;
        long duration;
        for(int i = 0; i < MAX_TIMERS; i++)
        {
            str += "Timer " + (i + 1) + ": Status = ";
            if(timers[i] != null && timers[i].isRunning())
            {
                if(timers[i].isRunning())
                {
                    listener = (TimerRolloverListener)timers[i].getActionListeners()[0];
                    duration = System.currentTimeMillis() - listener.getTime();
                    str += "Running, Channel = " + listener.getChannel().getName() +
                            ", Run Time = [" + DurationFormatUtils.formatDuration(duration, "HH:mm:ss") + "]/["
                            + DurationFormatUtils.formatDuration(timers[i].getDelay(), "HH:mm:ss") + "]";
                }
                else
                {
                    str = " Set to run for [" + DurationFormatUtils.formatDuration(timers[i].getDelay(), "HH:mm:ss") + "]";
                }

            }
            else
            {
                str += "Not Running ";
            }
        }

        return str;
    }

    private String timerCommandSet(MessageEvent event, ArrayList<String> params)
    {
        User sender = event.getUser();
        Channel channel = event.getChannel();

        Date date;
        int milliseconds = 0;
        Calendar calendar = Calendar.getInstance();

        switch(params.size())
        {
            case 0:
                return "[!timer set] Time parameter not given. Usage: !timer set <HH:mm:ss>";
            case 1:
                break;
            default:
                return "[!timer set] Too many parameters given: Usage !timer set <HH:mm:ss>";
        }

        //!timer set <HH:mm:ss>
        if(isMod(sender, channel))
        {
            try
            {
                for (int i = 0; i < timers.length; i++)
                {
                    if (timers[i] == null)
                    {
                        date = new SimpleDateFormat("HH:mm:ss").parse(params.get(0));
                        calendar.setTime(date);

                        milliseconds += HOURS_TO_MINUTES * MINUTES_TO_SECONDS * SECONDS_TO_MILLISECONDS * calendar.get(Calendar.HOUR);
                        milliseconds += MINUTES_TO_SECONDS * SECONDS_TO_MILLISECONDS * calendar.get(Calendar.MINUTE);
                        milliseconds += SECONDS_TO_MILLISECONDS * calendar.get(Calendar.SECOND);

                        timers[i] = new javax.swing.Timer(milliseconds, null);

                        return "Timer #" + (i + 1) + " successfully set for [" + params.get(0) + "]. " +
                                "Use !timer start " + (i + 1) + " to start the timer.";
                    }
                }

                return "[!timer set] I'm sorry, @" + sender.getNick() + ", but there are no timers available. " +
                        "Use !timer list to see which timers are currently in use";
            }
            catch(java.text.ParseException e)
            {
                return "[!timer set] I'm sorry, @" + sender.getNick() + " but " + params.get(0) + " is not a valid time";
            }
        }

        return "[!timer set] I'm sorry, @" + sender.getNick() + " but only mods can set timers";
    }

    @Override
    public void onMessage(MessageEvent event)
    {
        String s, response = "";
        User sender = event.getUser();
        Channel channel = event.getChannel();

        //Split message into words using a blank space as a delimiter
        ArrayList<String> params = new ArrayList<>(Arrays.asList(event.getMessage().split(" ")));
        s = params.remove(0);

        //Universal command operator
        if(s.startsWith("!"))
        {
            //Remove the ! from the command
            s = s.substring(1, s.length());
            switch (s)
            {
                /*case "giveaway":
                    response = giveawayCommand(event, params);
                    break;*/
                case "time":
                    response = "The time is now " + new java.util.Date().toString();
                    break;
                case "slap":
                    response = slapCommand(event, params);
                    break;
                case "quote":
                    response = quoteCommand(event, params);
                    break;
                case "":
                    break;
                case "raid":
                    response = raidCommand(event, params);
                    break;
                case "server":
                    response = serverCommand(event, params);
                    break;
                case "twitter":
                    response = "Follow Jdog on Twitter: https://twitter.com/Jdog653Speedrun";
                    break;
                case "facebook":
                    response = "We have a Facebook group! Find it here: https://www.facebook.com/Jdog653";
                    break;
                case "youtube":
                    response = "Subscribe to me on YouTube: https://www.youtube.com/c/Jdog653Speedruns";
                    break;
                case "steam":
                    response = "Join the Steam group! http://steamcommunity.com/groups/Jdog653";
                    break;
                case "donate":
                    response = "Donating is completely optional, but if you're so inclined you donate here: https://www.twitchalerts.com/donate/jdog653";
                    break;
                case "pb":
                    response = "";
                    break;
                case "money":
                    response = moneyCommand(event, params);
                    break;
                case "shoutout":
                    response = shoutoutCommand(event, params);
                    break;
                case "marathon":
                    //response = "The 24-hour+ marathon is ongoing! A list games played can be found here: http://pastebin.com/jUwXMbX6";
                    //break;
                case "discord":
                    response = "We have a Discord server! Join and you can talk with me! https://discord.gg/0V1Je0ht7gKejPGQ";
                    break;
                case "timer":
                    response = timerCommand(event, params);
                    break;
                default:
                    response = s + " is not recognized as a command. Perhaps you misspelled it?";
                    break;
            }

            event.getChannel().send().message(response);
        }
        else
        {
            if(giveawayActive)
            {
                if(event.getMessage().equalsIgnoreCase(giveawayPhrase) && !giveawayEntrants.contains(sender.getNick()))
                {
                    giveawayEntrants.add(sender.getNick());
                }
            }
        }
    }
}

    