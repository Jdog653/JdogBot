import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class JdogBotMain
{
    public static void main(String[] args) throws Exception
    {
        BufferedReader reader;
        Configuration.Builder builder = new Configuration.Builder();
        Configuration configuration;

        try
        {
            reader = new BufferedReader(new FileReader("JdogBot.txt"));
            String username, oauth, str;

            username = reader.readLine();
            oauth = reader.readLine();

            System.out.println("Logging in as " + username + " with password " + oauth);
            builder.setName(username);
            builder.setServer("irc.twitch.tv", 6667);
            builder.setServerPassword(oauth);

            //Skip the blank lines
            reader.readLine();
            reader.readLine();
            str = reader.readLine();
            while(str != null)
            {
                builder.addAutoJoinChannel(str);
                System.out.println("Added autojoin of: " + str);
                str = reader.readLine();
            }

            builder.addListener(new MessageListener());
            configuration = builder.buildConfiguration();

            //Create our bot with the configuration
            PircBotX bot = new PircBotX(configuration);
            //Connect to the server
            bot.startBot();
        }
        catch(FileNotFoundException e)
        {
            JOptionPane.showMessageDialog(null, "File \"JdogBot.txt\" is not present in the root directory. Please ensure it is present");
        }
    }
}