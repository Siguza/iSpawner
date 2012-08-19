// Bukkit Plugin "iSpawner" by Siguza
// This software is distributed under the following license:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.ispawner;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.logging.Logger;
import java.net.*;

import net.minecraft.server.*;

import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.*;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.scheduler.CraftScheduler;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.configuration.file.*;

import static net.drgnome.ispawner.Util.*;

public class DerpPlugin extends JavaPlugin implements Listener
{
    public static final String version = "1.0.0";
    private HashMap<String, TileEntityMobSpawner> map;
    private ArrayList<String> waiting;
    private Map<String, Class> eList;
    private int upTick;
    private boolean update;
    
    public DerpPlugin()
    {
        super();
        try
        {
            Field field = EntityTypes.class.getDeclaredField("b"); // Derpnote
            field.setAccessible(true);
            Object o = field.get(null);
            eList = (Map<String, Class>)o;
        }
        catch(Throwable t)
        {
            warn();
            t.printStackTrace();
            eList = new HashMap<String, Class>();
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void handleLogin(PlayerLoginEvent event)
    {
        if((!update) || (event == null) || (event.getPlayer() == null))
        {
            return;
        }
        Player player = event.getPlayer();
        if(!player.hasPermission("ispawner.use"))
        {
            return;
        }
        sendMessage(player, "There is an update for iSpawner available!", ChatColor.GOLD);
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void handleClickEvent(PlayerInteractEvent event)
    {
        if((event == null) || !event.hasBlock() || ((event.getAction() != Action.LEFT_CLICK_BLOCK) && (event.getAction() != Action.RIGHT_CLICK_BLOCK)))
        {
            return;
        }
        if(!waiting.remove(event.getPlayer().getName()))
        {
            return;
        }
        org.bukkit.World w = event.getClickedBlock().getWorld();
        if(!(w instanceof CraftWorld))
        {
            return;
        }
        TileEntity t = ((CraftWorld)w).getHandle().getTileEntity(event.getClickedBlock().getX(), event.getClickedBlock().getY(), event.getClickedBlock().getZ());
        if((t == null) || !(t instanceof TileEntityMobSpawner))
        {
            sendMessage(event.getPlayer(), "That's not a mob spawner.", ChatColor.RED);
            return;
        }
        TileEntityMobSpawner m = (TileEntityMobSpawner)t;
        m.spawnDelay = Integer.MAX_VALUE;
        setTE(event.getPlayer().getName(), m);
        sendMessage(event.getPlayer(), "Editing session started.", ChatColor.GREEN);
    }
    
    public void onEnable()
    {
        log.info("Enabling iSpawner " + version);
        upTick = 60 * 60 * 20;
        update = false;
        map = new HashMap<String, TileEntityMobSpawner>();
        waiting = new ArrayList<String>();
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getScheduler().scheduleSyncRepeatingTask(this, new DerpThread(this), 0L, 1L);
    }

    public void onDisable()
    {
        getServer().getScheduler().cancelTasks(this);
        for(TileEntityMobSpawner m : map.values().toArray(new TileEntityMobSpawner[0]))
        {
            if(m == null)
            {
                continue;
            }
            NBTTagCompound spawnData = (NBTTagCompound)get(m, "spawnData");
            if(spawnData != null)
            {
                World world = null;
                try
                {
                    Field field = TileEntity.class.getDeclaredField("world");
                    field.setAccessible(true);
                    world = (World)field.get(m);
                }
                catch(Throwable t)
                {
                    continue;
                }
                Entity entity = EntityTypes.createEntityByName(m.mobName, world);
                NBTTagCompound nbttagcompound = new NBTTagCompound();
                entity.c(nbttagcompound); // Derpnote
                Iterator iterator = spawnData.c().iterator(); // Derpnote
                while(iterator.hasNext())
                {
                    NBTBase nbtbase = (NBTBase)iterator.next();
                    nbttagcompound.set(nbtbase.getName(), nbtbase.clone());
                }
                try
                {
                    entity.e(nbttagcompound);
                }
                catch(Throwable t)
                {
                    continue;
                }
            }
            m.spawnDelay = 0;
        }
        map = new HashMap<String, TileEntityMobSpawner>();
        waiting = new ArrayList<String>();
        log.info("Disabling iSpawner " + version);
    }
    
    public synchronized void tick()
    {
        if(!update)
        {
            upTick++;
            if(upTick >= 60 * 60 * 20)
            {
                checkForUpdate();
                upTick = 0;
            }
        }
    }
    
    private void checkForUpdate()
    {
        try
        {
            HttpURLConnection con = (HttpURLConnection)(new URL("http://dev.drgnome.net/version.php?t=ispawner")).openConnection();            
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; JVM)");                        
            con.setRequestProperty("Pragma", "no-cache");
            con.connect();
            BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String line = null;
            StringBuilder stringb = new StringBuilder();
            if((line = reader.readLine()) != null)
            {
                stringb.append(line);
            }
            String vdigits[] = this.version.toLowerCase().split("\\.");
            String cdigits[] = stringb.toString().toLowerCase().split("\\.");
            int max = vdigits.length > cdigits.length ? cdigits.length : vdigits.length;
            int a = 0;
            int b = 0;
            for(int i = 0; i < max; i++)
            {
                try
                {
                    a = Integer.parseInt(cdigits[i]);
                }
                catch(Throwable t1)
                {
                    char c[] = cdigits[i].toCharArray();
                    for(int j = 0; j < c.length; j++)
                    {
                        a += (c[j] << ((c.length - (j + 1)) * 8));
                    }
                }
                try
                {
                    b = Integer.parseInt(vdigits[i]);
                }
                catch(Throwable t1)
                {
                    char c[] = vdigits[i].toCharArray();
                    for(int j = 0; j < c.length; j++)
                    {
                        b += (c[j] << ((c.length - (j + 1)) * 8));
                    }
                }
                if(a > b)
                {
                    update = true;
                    break;
                }
                else if(a < b)
                {
                    update = false;
                    break;
                }
                else if((i == max - 1) && (cdigits.length > vdigits.length))
                {
                    update = true;
                    break;
                }
            }
        }
        catch(Throwable t)
        {
        }
    }
    
    private void setTE(String name, TileEntityMobSpawner te)
    {
        map.put(name.toLowerCase(), te);
    }
    
    private boolean hasTE(String name)
    {
        return map.get(name.toLowerCase()) != null;
    }
    
    private TileEntityMobSpawner getTE(String name)
    {
        return map.get(name.toLowerCase());
    }
    
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if((sender instanceof Player) && !sender.hasPermission("ispawner.use"))
        {
            sendMessage(sender, "Sorry buddy, you're not allowed to use this command.", ChatColor.RED);
            return true;
        }
        if(update)
        {
            sendMessage(sender, "There is an update for iSpawner available!", ChatColor.GOLD);
        }
        if((args.length <= 0) || (args[0].equalsIgnoreCase("help")))
        {
            sendMessage(sender, "/edit version - Shows the current version", ChatColor.AQUA);
            sendMessage(sender, "/edit list - List all possible mobs", ChatColor.AQUA);
            sendMessage(sender, "/edit start - Start editing a mob spawner when you click it", ChatColor.AQUA);
            sendMessage(sender, "/edit start [x y z] - Start editing a mob spawner", ChatColor.AQUA);
            sendMessage(sender, "/edit info - Show everything about this mob spawner", ChatColor.AQUA);
            sendMessage(sender, "/edit mob [name] - Set the spawned mob", ChatColor.AQUA);
            sendMessage(sender, "/edit amount [amount] - Set the amount of spawned mobs", ChatColor.AQUA);
            sendMessage(sender, "/edit delay [min] [max] - Set the min and max spawning delay", ChatColor.AQUA);
            sendMessage(sender, "/edit data [path] [type] [value] - Set the mob data", ChatColor.AQUA);
            sendMessage(sender, "/edit data [path] (list/compound/-) - Set to list/compound or delete", ChatColor.AQUA);
            sendMessage(sender, "/edit end - Finish the editing session", ChatColor.AQUA);
            return true;
        }
        String c = args[0].toLowerCase();
        if(c.equals("version"))
        {
            sendMessage(sender, "iSpawner version: " + version, ChatColor.GREEN);
            return true;
        }
        if(c.equals("list"))
        {
            mobList(sender);
            return true;
        }
        if(!(sender instanceof CraftPlayer))
        {
            sendMessage(sender, "Sorry buddy, only players can use this command.", ChatColor.RED);
            return true;
        }
        String name = sender.getName();
        if(c.equals("start"))
        {
            if(args.length >= 4)
            {
                try
                {
                    int x = Integer.parseInt(args[1]);
                    int y = Integer.parseInt(args[2]);
                    int z = Integer.parseInt(args[3]);
                    TileEntity t = ((CraftPlayer)sender).getHandle().world.getTileEntity(x, y, z);
                    if((t == null) || !(t instanceof TileEntityMobSpawner))
                    {
                        sendMessage(sender, "That's not a mob spawner.", ChatColor.RED);
                        return true;
                    }
                    TileEntityMobSpawner m = (TileEntityMobSpawner)t;
                    m.spawnDelay = Integer.MAX_VALUE;
                    setTE(name, m);
                    sendMessage(sender, "Editing session started.", ChatColor.GREEN);
                    return true;
                }
                catch(Throwable t)
                {
                    sendMessage(sender, "Error parsing coordinates.", ChatColor.RED);
                    return true;
                }
            }
            if(args.length > 1)
            {
                sendMessage(sender, "You need to specify 3 coordinates or none.", ChatColor.RED);
                return true;
            }
            waiting.add(name);
            sendMessage(sender, "Now click on a mob spawner.", ChatColor.GREEN);
            return true;
        }
        if(!hasTE(name))
        {
            sendMessage(sender, "Select a mob spawner first.", ChatColor.RED);
            return true;
        }
        TileEntityMobSpawner m = getTE(name);
        if(c.equals("end"))
        {
            NBTTagCompound spawnData = (NBTTagCompound)get(m, "spawnData");
            if(spawnData != null)
            {
                Entity entity = EntityTypes.createEntityByName(m.mobName, ((CraftPlayer)sender).getHandle().world);
                NBTTagCompound nbttagcompound = new NBTTagCompound();
                entity.c(nbttagcompound); // Derpnote
                Iterator iterator = spawnData.c().iterator(); // Derpnote
                while(iterator.hasNext())
                {
                    NBTBase nbtbase = (NBTBase)iterator.next();
                    nbttagcompound.set(nbtbase.getName(), nbtbase.clone());
                }
                try
                {
                    entity.e(nbttagcompound);
                }
                catch(Throwable t)
                {
                    sendMessage(sender, "Error parsing entity data.", ChatColor.RED);
                    sendMessage(sender, t.getMessage(), ChatColor.YELLOW);
                    return true;
                }
            }
            m.spawnDelay = 0;
            setTE(name, null);
            sendMessage(sender, "Session ended.", ChatColor.GREEN);
        }
        else if(c.equals("info"))
        {
            sendMessage(sender, "Mob: " + m.mobName, ChatColor.GREEN);
            sendMessage(sender, "Amount: " + get(m, "spawnCount").toString(), ChatColor.GREEN);
            sendMessage(sender, "Delay: " + get(m, "minSpawnDelay").toString() + " - " + get(m, "maxSpawnDelay").toString(), ChatColor.GREEN);
            for(String s : printNBT("spawnData", (NBTBase)get(m, "spawnData")))
            {
                sendMessage(sender, s, ChatColor.GREEN);
            }
        }
        else if(c.equals("mob"))
        {
            if(args.length < 2)
            {
                mobList(sender);
                return true;
            }
            String mob = args[1];
            if(!checkMob(mob))
            {
                sendMessage(sender, "This mob name can't be used in a spawner.", ChatColor.RED);
                return true;
            }
            m.mobName = mob;
            sendMessage(sender, "Mob set.", ChatColor.GREEN);
        }
        else if(c.equals("amount"))
        {
            if(args.length < 2)
            {
                sendMessage(sender, "Too few arguments.", ChatColor.RED);
                return true;
            }
            try
            {
                if(set(m, "spawnCount", Integer.parseInt(args[1])))
                {
                    sendMessage(sender, "Set the spawn amount.", ChatColor.GREEN);
                }
                else
                {
                    sendMessage(sender, "Couldn't set the spawn amount, sorry.", ChatColor.YELLOW);
                }
            }
            catch(Throwable t)
            {
                sendMessage(sender, "That's not an integer.", ChatColor.RED);
            }
        }
        else if(c.equals("delay"))
        {
            if(args.length < 3)
            {
                sendMessage(sender, "Too few arguments.", ChatColor.RED);
                return true;
            }
            try
            {
                int min = Integer.parseInt(args[1]);
                int max = Integer.parseInt(args[2]);
                if(max <= min)
                {
                    sendMessage(sender, "Max must be bigger than min.", ChatColor.RED);
                    return true;
                }
                if(set(m, "minSpawnDelay", min) && set(m, "maxSpawnDelay", max))
                {
                    sendMessage(sender, "Set the delay times.", ChatColor.GREEN);
                }
                else
                {
                    sendMessage(sender, "Couldn't set the delay times, sorry.", ChatColor.YELLOW);
                }
            }
            catch(Throwable t)
            {
                sendMessage(sender, "Error parsing the parameters", ChatColor.RED);
            }
        }
        else if(c.equals("data"))
        {
            if(args.length < 4 && ((args.length < 3) || (!args[2].equals("-") && !args[2].equalsIgnoreCase("list") && !args[2].equalsIgnoreCase("compound"))))
            {
                sendMessage(sender, "Too few arguments.", ChatColor.RED);
                return true;
            }
            String value = "";
            if(!args[2].equals("-") && !args[2].equalsIgnoreCase("list") && !args[2].equalsIgnoreCase("compound"))
            {
                value = args[3];
            }
            Object o = get(m, "spawnData");
            NBTTagCompound base;
            if((o != null) && (o instanceof NBTTagCompound))
            {
                base = (NBTTagCompound)o;
            }
            else
            {
                base = new NBTTagCompound("SpawnData");
            }
            String string = setNBT(base, args[1], args[2].toLowerCase(), value);
            if(string.length() > 0)
            {
                sendMessage(sender, string, ChatColor.RED);
                return true;
            }
            set(m, "spawnData", base);
            sendMessage(sender, "Data set.", ChatColor.GREEN);
        }
        else
        {
            sendMessage(sender, "Unknown command, use /edit help", ChatColor.RED);
        }
        return true;
    }
    
    private void mobList(CommandSender sender)
    {
        sendMessage(sender, "Possible mobs are:", ChatColor.AQUA);
        String string = "";
        for(String mob : eList.keySet().toArray(new String[0]))
        {
            if(checkMob(mob))
            {
                string += mob + ", ";
            }
        }
        sendMessage(sender, string, ChatColor.AQUA);
    }
    
    private boolean checkMob(String mob)
    {
        Class c = eList.get(mob);
        if(c == null)
        {
            return false;
        }
        return ((c.getModifiers() & 1024) == 0) && hasSuperclass(c, EntityLiving.class);
    }
}