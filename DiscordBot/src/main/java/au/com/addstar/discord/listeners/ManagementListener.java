package au.com.addstar.discord.listeners;

import au.com.addstar.discord.SimpleBot;
import au.com.addstar.discord.managers.UserManager;
import au.com.addstar.discord.objects.GuildConfig;
import au.com.addstar.discord.objects.McUser;
import au.com.addstar.discord.ulilities.Utility;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.events.guild.member.UserLeaveEvent;
import sx.blah.discord.handle.impl.events.guild.member.UserJoinEvent;
import sx.blah.discord.handle.impl.events.user.PresenceUpdateEvent;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IPresence;
import sx.blah.discord.handle.obj.IUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 9/12/2016.
 */
public class ManagementListener {

    SimpleBot bot =  SimpleBot.instance;

    @EventSubscriber
    public void onReadyEvent(ReadyEvent event){
        IDiscordClient client = event.getClient(); // Gets the client from the event object
        for (IGuild guild : client.getGuilds()){
            Long id = guild.getLongID();
            GuildConfig config  =  new GuildConfig(id);
            bot.addGuild(id, config);
        }
        UserManager.initialize(SimpleBot.client);
    }

    @EventSubscriber
    public void onJoinEvent(UserJoinEvent event){
        IUser u = event.getUser();
        IGuild g = event.getGuild();
        GuildConfig config = bot.getGuildConfig(g.getLongID());
        McUser user = UserManager.loadUserFromFile(u.getLongID());
        if(user == null){
            user = new McUser(u.getLongID());
            UserManager.cacheUser(user);
        }
        Utility.sendPrivateMessage(u, config.getWelcomeMessage());
        Utility.sendChannelMessage(config.getAnnounceChannelID(), u.getDisplayName(g) + " has joined " + g.getName());
    }

    public void onLeaveEvent(UserLeaveEvent e){
        IUser u = e.getUser();
        IGuild g = e.getGuild();
        GuildConfig config = bot.getGuildConfig(g.getLongID());
        Utility.sendChannelMessage(config.getAnnounceChannelID(), u.getDisplayName(g) + " has left  " + g.getName());
    }


    @EventSubscriber
    public void onUserPresenceChange(PresenceUpdateEvent e){
        IPresence p = e.getNewPresence();
        IUser u = e.getUser();
        McUser user = UserManager.loadUser(u.getLongID());
        List<IGuild> userGuilds = new ArrayList<>();
        if(user != null){
            for(Map.Entry<Long, String> entry : user.getDisplayNames().entrySet()){
                IGuild guild = e.getClient().getGuildByID(entry.getKey());
                if (guild!=null)userGuilds.add(guild);
            }
        }else{
            SimpleBot.log.info("User : " + u.getName() + "has been cached with no mcUuid you need to run an update on that user.");
            user = new McUser(u.getLongID());
            List<IGuild> guilds = e.getClient().getGuilds();
            for(IGuild g : guilds){
            if(g.getUserByID(u.getLongID())!=null){
                userGuilds.add(g);
                UserManager.checkUserDisplayName(user,g);
            }
            UserManager.saveUser(user);
        }
        }
        String message = "";
        switch(p.getStatus()){
            case ONLINE:
                message = " has come online.";
                break;
            case IDLE:
                message = " has changed to idle";
                break;
            case DND:
                message = " has asked not to be disturbed";
                break;
            case OFFLINE:
                message = " is now offline,";
                break;
            default:
                message = null;
        }
        if(message != null) {
            for (IGuild g : userGuilds) {
                GuildConfig config = bot.getGuildConfig(g.getLongID());
                Long channelID = config.getModChannelID();
                Boolean report = config.isReportStatusChange();
                if (channelID != null && channelID > 0 && report) {
                    Utility.sendChannelMessage(channelID, u.getDisplayName(g) + message);
                } else {
                    SimpleBot.log.info(g.getName() + ": " + u.getDisplayName(g) + message);
                }
            }
        }
    }






}
