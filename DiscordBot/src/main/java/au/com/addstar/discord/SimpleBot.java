package au.com.addstar.discord;

import au.com.addstar.discord.http.AnnouncerHandler;
import au.com.addstar.discord.http.DefaultHandler;
import au.com.addstar.discord.http.InviteHandler;
import au.com.addstar.discord.listeners.CommandListener;
import au.com.addstar.discord.listeners.ManagementListener;
import au.com.addstar.discord.objects.GuildConfig;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 7/12/2016.
 */
public class SimpleBot {

    public static SimpleBot instance;
    public static IDiscordClient client;
    public static Properties config;
    private static HashMap<Long,GuildConfig> gConfigs;
    static HttpServer server;
    public static final Logger log = LoggerFactory.getLogger(SimpleBot.class);


    public SimpleBot(IDiscordClient client) {
        SimpleBot.client = client;
        gConfigs = new HashMap<>();
    }

    public static void main(String[] args) {
        config = Configuration.loadConfig();
        String token = config.getProperty("discordToken", null);
        if(token==null){
            SimpleBot.log.info("Server shut down initiated...");
            SimpleBot.log.info("You must edit the config.properties file and add your discord app private token.");
            System.exit(1);
        }
        instance = login(token);
        if(instance == null){
            SimpleBot.log.info("We could not instantiate a valid Bot....");
            close();
        }
        configureListeners();
        server = createHttpServer();
        addContexts(server);
        server.setExecutor(null);
        server.start();
        log.info("HttpServer started on " + server.getAddress().getHostString() +":"+ server.getAddress().getPort());
        Runtime.getRuntime().addShutdownHook(new Thread(SimpleBot::close, "Shutdown-thread"));
    }

    private static SimpleBot login(String token) {
        ClientBuilder builder = new ClientBuilder(); // Creates a new client builder instance
        builder.withToken(token);// Sets the bot token for the client
        builder.withRecommendedShardCount();
        builder.setDaemon(true);
        IDiscordClient c;
        c = builder.login(); // Builds the IDiscordClient instance and logs it in
            // Creating the bot instance
        if (c != null) {
            return new SimpleBot(c);
        }else{
            return null;
        }
    }

    private static void configureListeners() {
        ManagementListener mListen = new ManagementListener();
        CommandListener cListen = new CommandListener();
        client.getDispatcher().registerListener(mListen);
        client.getDispatcher().registerListener(cListen);
        log.info("Listeners are configured.");
    }

    private static HttpServer createHttpServer(){
        HttpServer server =null;
        String host = config.getProperty("hostnameIP","localhost");
        Integer port = Integer.parseInt(config.getProperty("httpPort","22000"));
        try {
            InetAddress ip = InetAddress.getByName(host);
            InetSocketAddress socketAddress = new InetSocketAddress(ip,port);
            server = HttpServer.create(socketAddress, 2);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return server;
    }

    private static void addContexts(HttpServer server){
        server.createContext("/", new DefaultHandler());
        server.createContext("/invite/", new InviteHandler());
        server.createContext("/announcer/", new AnnouncerHandler());
    }
    public static void exit(){
        System.exit(0);
    }


    private static void close() {
        SimpleBot.log.info("Server shut down initiated...");
        SimpleBot.log.info("Saving Guild Configs");
        for(Map.Entry<Long,GuildConfig> entry : SimpleBot.gConfigs.entrySet()){
            GuildConfig guildconfig = entry.getValue();
            guildconfig.saveConfig();
        }
        SimpleBot.log.info("GuildConfigs saved.");
    }

    public GuildConfig getGuildConfig(long id){
        return gConfigs.get(id);
    }

    public void addGuild(long id, GuildConfig config){
        gConfigs.put(id, config);
    }




}
