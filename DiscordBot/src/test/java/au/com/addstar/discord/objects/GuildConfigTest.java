package au.com.addstar.discord.objects;

import au.com.addstar.discord.managers.InvitationManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.net.URL;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 14/12/2016.
 */
public class GuildConfigTest {

    private GuildConfig testConfig;
    private File inviteFile;
    private Invitation invite;

    @Before
    public void setUp() throws Exception {
        createTestGuildConfigFile();
        createTestInviteJsonFile();
        Long id = 200000000000000000L;
        testConfig = new GuildConfig(id);
        invite = createNewInvitation("Test12345");

    }

    @Test
    public void testLoadConfig(){
        createTestGuildConfigFile();
        testConfig.loadConfig();
        Assert.assertEquals("TEST Server", testConfig.getWelcomeMessage());
        Assert.assertTrue(205586118752600064L == testConfig.getAnnounceChannelID());
        Assert.assertEquals("!!", testConfig.getPrefix());
    }

    @Test
    public void testSaveConfig() throws Exception {
        Assert.assertTrue(0L == testConfig.getModChannelID());
        Long id = 0L;
        testConfig.setModChannelID(id);
        testConfig.saveConfig();
        testConfig = null;
        GuildConfig testConfig2 = new GuildConfig(200000000000000000L);
        Assert.assertTrue( id == testConfig2.getModChannelID());
    }

    @Test
    public void testStoreInvite() throws Exception {
        createTestGuildConfigFile();
        createTestInviteJsonFile();
        InvitationManager.storeInvitation(testConfig, invite);
        Invitation retrieved = InvitationManager.getInvitation(testConfig,"Test12345");
        Assert.assertNull(retrieved);
        retrieved = InvitationManager.getExpiredInvite(testConfig,"Test12345");
        Assert.assertEquals(invite,retrieved);

    }


    @Test
    public void testRemoveInvite() throws Exception {
        InvitationManager.storeInvitation(testConfig,createNewInvitation("12345"));
        int size = testConfig.getInviteCache().size();
        InvitationManager.removeInvitation(testConfig,"12345");
        Assert.assertTrue(testConfig.getInviteCache().size() < size);
    }

    @Test
    public void testSaveInvites() throws Exception {
        inviteFile.delete();
        //assertFalse(inviteFile.exists());
        InvitationManager.storeInvitation(testConfig,createNewInvitation("65324"));
        InvitationManager.storeInvitation(testConfig,createNewInvitation("65555"));
        testConfig.saveConfig();
        Assert.assertTrue(inviteFile.exists());
        Assert.assertTrue(inviteFile.length() > 0);
    }

    @Test
    public void testLoadInvites() throws Exception {
        createTestGuildConfigFile();
        createTestInviteJsonFile();
        testConfig.getInviteCache().clear();
        testConfig.loadConfig();
        Assert.assertTrue(testConfig.getInviteCache().size() == 2);
    }

    private Invitation createNewInvitation(String code) {
        UUID uid = UUID.randomUUID();
        return new Invitation(uid, "Test Guild", 12356945L, code);
    }

    private void createTestGuildConfigFile() {
        File parent = new File("guilds");
        if (!parent.exists()) {
            parent.mkdir();
        }
        File outFile = new File(parent, "200000000000000000.properties");

        if(!outFile.delete()){
            try {
                throw new IOException("Could not delete file"){
                };
            } catch (IOException e) {
                Logger.getAnonymousLogger().info(e.getLocalizedMessage());
            }
        }
        try {
            outFile.createNewFile();
            OutputStream out = new FileOutputStream(outFile);
            URL url = this.getClass().getResource("/200000000000000000.properties");
            InputStream input = new FileInputStream(url.getFile());
            byte[] buffer = new byte[1024];
            int len = input.read(buffer);
            while (len != -1) {
                out.write(buffer, 0, len);
                len = input.read(buffer);
            }
            input.close();
            out.close();
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void createTestInviteJsonFile() {
        File parent = new File("guilds");
        File sub = new File(parent, "200000000000000000");
        if (!sub.exists()) {
            sub.mkdir();
        }
        inviteFile = new File(sub, "invites.json");
        if(!inviteFile.delete()){
            try {
                throw new IOException("Could not delete file"){
                };
            } catch (IOException e) {
                Logger.getAnonymousLogger().info(e.getLocalizedMessage());
            }
        }
        try {
            inviteFile.createNewFile();
            OutputStream out = new FileOutputStream(inviteFile);
            URL url = this.getClass().getResource("/invites.json");
            InputStream input = new FileInputStream(url.getFile());
            byte[] buffer = new byte[1024];
            int len = input.read(buffer);
            while (len != -1) {
                out.write(buffer, 0, len);
                len = input.read(buffer);
            }
            input.close();
            out.close();
        }catch (IOException | NullPointerException e){
            e.printStackTrace();
        }
    }
}