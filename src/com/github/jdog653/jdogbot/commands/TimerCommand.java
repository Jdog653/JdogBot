package com.github.jdog653.jdogbot.commands;

import com.github.jdog653.jdogbot.GlobalFunctions;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.pircbotx.Channel;
import org.pircbotx.User;
import org.pircbotx.hooks.events.MessageEvent;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import javax.swing.Timer;

/**
 * Created by jordan on 3/30/16.
 */
public class TimerCommand implements Command
{
    private final int MAX_TIMERS, HOURS_TO_MINUTES = 60, MINUTES_TO_SECONDS = 60, SECONDS_TO_MILLISECONDS = 1000;

    private class TimerRolloverListener implements ActionListener
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

    private String baseCommandName;
    private Timer[] timers;

    public TimerCommand(String base, final int numTimers)
    {
        baseCommandName = base;
        MAX_TIMERS = numTimers;

        timers = new Timer[MAX_TIMERS];

        for(int i = 0; i < MAX_TIMERS; i++)
        {
            timers[i] = null;
        }
    }

    //!timer set <HH:mm:ss>
    //!timer start <timer number>
    //!timer status <timer number>
    //!timer stop <timer number>
    //!timer clear <timer number>
    //!timer list
    @Override
    public String executeCommand(MessageEvent event, ArrayList<String> params)
    {
        switch(params.size())
        {
            case 0:
                return timerCommandList(event, params);
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
                        return "[!timer] Parameter " + params.get(0) + " is not a valid parameter";

                }
            case 1:
                switch(params.get(0))
                {
                    case "list":
                        return timerCommandList(event, params);
                    default:
                        return "[!timer] Parameter " + params.get(0) + " is not a valid parameter";
                }
            default:
                return "[!timer] Too many parameters given. For usage, use !help timer";
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

        if(GlobalFunctions.isMod(sender, channel))
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

        if(GlobalFunctions.isMod(sender, channel))
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

        if(GlobalFunctions.isMod(sender, channel))
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
            if(timers[i] != null)
            {
                if(timers[i].isRunning())
                {
                    listener = (TimerRolloverListener)timers[i].getActionListeners()[0];
                    duration = System.currentTimeMillis() - listener.getTime();
                    str += "Running, Channel = " + listener.getChannel().getName() +
                            ", Run Time = [" + DurationFormatUtils.formatDuration(duration, "HH:mm:ss") + "]/["
                            + DurationFormatUtils.formatDuration(timers[i].getDelay(), "HH:mm:ss") + "] ";
                }
                else
                {
                    str = " Set to run for [" + DurationFormatUtils.formatDuration(timers[i].getDelay(), "HH:mm:ss") + "] ";
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
        if(GlobalFunctions.isMod(sender, channel))
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
    public String getBaseCommandName()
    {
        return baseCommandName;
    }
}
