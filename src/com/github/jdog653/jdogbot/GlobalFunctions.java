package com.github.jdog653.jdogbot;

import com.github.jdog653.jdogbot.commands.Command;
import org.pircbotx.Channel;
import org.pircbotx.User;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

/**
 * Created by jordan on 3/26/16.
 */
public class GlobalFunctions
{
    public static final ArrayList<String> MODS = new ArrayList<>(Arrays.asList("jdog653","theofficialskozzy", "jdogbot", "mollyranchers"));

    public static void writeListToFile(final String fileName, final ArrayList<String> list)
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

    public static void fileInit(final String fileName, ArrayList<String> list)
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
                reader.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public static boolean isMod(User sender, Channel channel)
    {
        return channel.getOps().contains(sender) || MODS.contains(sender.getNick().toLowerCase());
    }

    public static boolean isChannelOwner(User sender, Channel channel)
    {
        return sender.getNick().equalsIgnoreCase(channel.getName().replace("#", ""));
    }

    public static boolean isCommand(String c)
    {
        for(Command.CurrentCommands command : Command.CurrentCommands.values())
        {
            if(c.equals(command.name().toLowerCase()))
            {
                return true;
            }
        }
        return false;
    }
}
