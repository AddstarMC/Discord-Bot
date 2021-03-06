package au.com.addstar.discord.managers;

import au.com.addstar.discord.SimpleBot;
import au.com.addstar.discord.objects.McUser;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

import java.io.*;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 18/12/2016.
 */
public class UserManager {

    private static Map<Long, McUser> userCache = new HashMap<>();
    private static Gson gsonencoder = new Gson();
    private static Type type = new TypeToken<McUser>(){}.getType();

    public static void initialize(IDiscordClient client){
        List<IGuild> guilds = client.getGuilds();
        for (IGuild guild : guilds){
            List<IUser> users = guild.getUsers();
            for(IUser user : users){
                McUser mcUser = loadUserFromFile(user.getLongID());
                if (mcUser == null){
                    SimpleBot.log.info("No save exists for User:" + user.getStringID());
                    mcUser = new McUser(user.getLongID());
                    mcUser.addUpdateDisplayName(guild.getLongID(),user.getDisplayName(guild));
                    saveUserToFile(mcUser);
                }
                cacheUser(mcUser);
            }
        }
    }



    public static void addGuildtoUser(McUser user, String displayName, IGuild guild) {
        user.addUpdateDisplayName(guild.getLongID(),displayName);
    }

    public static void cacheUser(McUser u) {
        Long key = u.getDiscordID();
        userCache.put(key, u);
    }

    private static McUser loadfromCache(Long id){
        return userCache.get(id);
    }

    public static void removeUser(McUser u) {
        userCache.remove(u.getDiscordID());

    }
    public static void saveUser(McUser u){
        cacheUser(u);
        saveUserToFile(u);
    }

    public static void saveCache() {
        SimpleBot.log.info("Transferring User Cache to disk");
        File parent = new File("users");
        if (!parent.exists()) {
            parent.mkdir();
        }
        for (Map.Entry<Long, McUser> e : userCache.entrySet()) {
            String fileName = e.getKey() + ".json";
            File outFile = new File(parent, fileName);
            boolean exists = outFile.exists();
            if (!exists) {
                try {
                    outFile.createNewFile();
                } catch (IOException ex) {
                    SimpleBot.log.error("Could not create file :" + outFile.getName());
                    continue;
                }
            }
            try {
                OutputStream out = new FileOutputStream(outFile);
                String encoded = gsonencoder.toJson(e.getValue(), type);
                out.write(encoded.getBytes());
                out.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        SimpleBot.log.info("User Cache saved to disk");


    }
    public static void saveUserToFile(McUser u) {
        SimpleBot.log.info("Transferring User to disk: " + u.getDiscordID());
        File parent = new File("users");
        if (!parent.exists()) {
            parent.mkdir();
        }
        String fileName = u.getDiscordID() + ".json";
        File outFile = new File(parent, fileName);
        boolean exists = outFile.exists();
        if (!exists) {
            try {
                outFile.createNewFile();
            } catch (IOException ex) {
                SimpleBot.log.error("Could not create file :" + outFile.getName());
                return;
            }
        }
        try {
            OutputStream out = new FileOutputStream(outFile);
            Type type = new TypeToken<McUser>() {
            }.getType();
            String encoded = gsonencoder.toJson(u, type);
            out.write(encoded.getBytes());
            out.close();
        } catch (IOException ex) {
            SimpleBot.log.error("Unable to write user file to disk");
            ex.printStackTrace();
            return;
        }

        SimpleBot.log.info("User saved to disk");

    }
    public static McUser loadUser(Long id){
        McUser user = loadfromCache(id);
        return(user == null)?loadUserFromFile(id):user;
    }
    public static McUser loadUserFromFile(Long id){
        File parent = new File("users");
        if(!parent.exists()){
            return null;
        }
        String fileName = id + ".json";
        File file = new File(parent,fileName);
        if(!file.exists())return null;
        try {
            InputStream in = new FileInputStream(file);
            InputStreamReader inread = new InputStreamReader(in);
            McUser user = gsonencoder.fromJson(inread,type);
            userCache.put(user.getDiscordID(),user);
            return user;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;

    }

    public static void setUserNick(IGuild g, IUser u, String nick){
        try {
            g.setUserNickname(u,nick);
        } catch (MissingPermissionsException e) {
            SimpleBot.log.error(" We dont have permission to set the nick of " + u.getDisplayName(g));
        } catch (DiscordException | RateLimitException e) {
            e.printStackTrace();
        }
    }

    public static void setRoleforUser(IGuild g, IUser u, IRole r){
        List<IRole> currroles = g.getRolesForUser(u);
        currroles.add(r);
        IRole[] newroles = currroles.toArray(new IRole[0]);
        try {
           g.editUserRoles(u,newroles);
        } catch (MissingPermissionsException e) {
            SimpleBot.log.error(" We dont have permission to set the role of " + u.getDisplayName(g) + " to " + Arrays.toString(newroles));
            e.printStackTrace();
        } catch (RateLimitException | DiscordException e) {
            e.printStackTrace();
        }
    }

    public static void checkUserDisplayName(McUser user, IGuild guild){
        String savedName = user.getDisplayName(guild.getLongID());
        String currName = guild.getUserByID(user.getDiscordID()).getDisplayName(guild);
        if(savedName != null){
        if(savedName != currName){
            SimpleBot.log.info("Discord User: " + user.getDiscordID() + " has updated thier displayName. Resetting");
            //todo hook back and check for update in MC
            IUser discordUser = guild.getUserByID(user.getDiscordID());
            setUserNick(guild,discordUser,savedName);
            }
        }else{
            user.addUpdateDisplayName(guild.getLongID(),currName);
            saveUser(user);
        }
    }



}
