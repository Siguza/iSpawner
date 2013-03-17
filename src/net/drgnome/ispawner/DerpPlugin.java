// Bukkit Plugin "iSpawner" by Siguza
// This software is distributed under the following license:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.ispawner;

import java.lang.reflect.*;
import java.util.*;
import java.util.logging.Logger;

import net.minecraft.server.v#MC_VERSION#.*;

import org.bukkit.craftbukkit.v#MC_VERSION#.CraftWorld;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.*;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v#MC_VERSION#.entity.CraftPlayer;
import org.bukkit.craftbukkit.v#MC_VERSION#.scheduler.CraftScheduler;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.configuration.file.*;

import static net.drgnome.ispawner.Config.*;
import static net.drgnome.ispawner.Util.*;

public class DerpPlugin extends JavaPlugin implements Listener, Runnable
{
    public static final String _version = "#VERSION#";
    private HashMap<String, MobSpawnerAbstract> map = new HashMap<String, MobSpawnerAbstract>();
    private ArrayList<String> waiting = new ArrayList<String>();
    private Map<String, Class> eList;
    private boolean _update = false;
    
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
        if(event.getPlayer().hasPermission("ispawner.use"))
        {
            sendMessage(event.getPlayer(), "There is an update for iSpawner available!", ChatColor.GOLD);
        }
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
        TileEntityMobSpawner mob = (TileEntityMobSpawner)t;
        MobSpawnerAbstract m = (MobSpawnerAbstract)get(mob, "a");
        m.spawnDelay = Integer.MAX_VALUE;
        setTE(event.getPlayer().getName(), m);
        sendMessage(event.getPlayer(), "Editing session started.", ChatColor.GREEN);
    }
    
    public void reloadConfig()
    {
        super.reloadConfig();
        reloadConf(getConfig());
        saveConfig();
    }
    
    public void onEnable()
    {
        log.info("Enabling iSpawner " + _version);
        reloadConf(getConfig());
        saveConfig();
        getServer().getPluginManager().registerEvents(this, this);
        if(getConfigString("check-update").equalsIgnoreCase("true"))
        {
            getServer().getScheduler().scheduleSyncRepeatingTask(this, this, 0L, 72000L);
        }
    }

    public void onDisable()
    {
        getServer().getScheduler().cancelTasks(this);
        for(MobSpawnerAbstract m : map.values())
        {
            if(m == null)
            {
                continue;
            }
            NBTTagCompound spawnData = getSpawnData(m);
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
                Entity entity = EntityTypes.createEntityByName(m.getMobName(), world);
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
        map = new HashMap<String, MobSpawnerAbstract>();
        waiting = new ArrayList<String>();
        log.info("Disabling iSpawner " + _version);
    }
    
    private void setTE(String name, MobSpawnerAbstract te)
    {
        map.put(name.toLowerCase(), te);
    }
    
    private boolean hasTE(String name)
    {
        return map.get(name.toLowerCase()) != null;
    }
    
    private MobSpawnerAbstract getTE(String name)
    {
        return map.get(name.toLowerCase());
    }
    
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        try
        {
            if((sender instanceof Player) && !sender.hasPermission("ispawner.use"))
            {
                sendMessage(sender, "Sorry buddy, you're not allowed to use this command.", ChatColor.RED);
                return true;
            }
            if(_update)
            {
                sendMessage(sender, "There is an update for iSpawner available!", ChatColor.GOLD);
            }
            if((args.length <= 0) || (args[0].equalsIgnoreCase("help")))
            {
                sendMessage(sender, "/edit version - Shows the current version", ChatColor.AQUA);
                sendMessage(sender, "/edit list - List the normally possible mobs", ChatColor.AQUA);
                sendMessage(sender, "/edit list all - List ALL possible mobs", ChatColor.AQUA);
                sendMessage(sender, "/edit defval - List the default values", ChatColor.AQUA);
                sendMessage(sender, "/edit start - Start editing a mob spawner when you click it", ChatColor.AQUA);
                sendMessage(sender, "/edit start [x y z] - Start editing a mob spawner", ChatColor.AQUA);
                sendMessage(sender, "/edit info - Show everything about this mob spawner", ChatColor.AQUA);
                sendMessage(sender, "/edit mob [name] - Set the spawned mob", ChatColor.AQUA);
                sendMessage(sender, "/edit mob -f [name] - Force setting the spawned mob", ChatColor.AQUA);
                sendMessage(sender, "/edit amount [amount] - Set the amount of spawned mobs", ChatColor.AQUA);
                sendMessage(sender, "/edit delay [min] [max] - Set the min and max spawning delay", ChatColor.AQUA);
                sendMessage(sender, "/edit data [path] [type] [value] - Set the mob data", ChatColor.AQUA);
                sendMessage(sender, "/edit data [path] (list/compound/-) - Set to list/compound or delete", ChatColor.AQUA);
                sendMessage(sender, "/edit max [max] - Spawn no more creatures if [max] of them are in range", ChatColor.AQUA);
                sendMessage(sender, "/edit range [range] - Spawn and search for creatures in this range", ChatColor.AQUA);
                sendMessage(sender, "/edit playerrange [range] - Only spawn creatures if a player is within this range", ChatColor.AQUA);
                sendMessage(sender, "/edit end - Finish the editing session", ChatColor.AQUA);
                return true;
            }
            String c = args[0].toLowerCase();
            if(c.equals("version"))
            {
                sendMessage(sender, "iSpawner version: " + _version, ChatColor.GREEN);
                return true;
            }
            if(c.equals("list"))
            {
                mobList(sender, (args.length >= 2) && args[1].equalsIgnoreCase("all"));
                return true;
            }
            if(c.equals("defval"))
            {
                sendMessage(sender, "mob: Pig", ChatColor.GREEN);
                sendMessage(sender, "amount: 4", ChatColor.GREEN);
                sendMessage(sender, "delay: 200 800", ChatColor.GREEN);
                sendMessage(sender, "max: 6", ChatColor.GREEN);
                sendMessage(sender, "range: 4", ChatColor.GREEN);
                sendMessage(sender, "playerrange: 16", ChatColor.GREEN);
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
                        TileEntityMobSpawner mob = (TileEntityMobSpawner)t;
                        MobSpawnerAbstract m = (MobSpawnerAbstract)get(mob, "a");
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
            MobSpawnerAbstract m = getTE(name);
            if(c.equals("end"))
            {
                NBTTagCompound spawnData = getSpawnData(m);
                if(spawnData != null)
                {
                    Entity entity = EntityTypes.createEntityByName(m.getMobName(), ((CraftPlayer)sender).getHandle().world);
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
                        t.printStackTrace();
                        return true;
                    }
                }
                m.spawnDelay = 0;
                setTE(name, null);
                sendMessage(sender, "Session ended.", ChatColor.GREEN);
            }
            else if(c.equals("info"))
            {
                sendMessage(sender, "Mob: " + m.getMobName(), ChatColor.GREEN);
                sendMessage(sender, "Amount: " + get(MobSpawnerAbstract.class, m, "spawnCount").toString(), ChatColor.GREEN);
                sendMessage(sender, "Delay: " + get(MobSpawnerAbstract.class, m, "minSpawnDelay").toString() + " - " + get(MobSpawnerAbstract.class, m, "maxSpawnDelay").toString(), ChatColor.GREEN);
                sendMessage(sender, "Max: " + get(MobSpawnerAbstract.class, m, "maxNearbyEntities").toString(), ChatColor.GREEN);
                sendMessage(sender, "Range: " + get(MobSpawnerAbstract.class, m, "spawnRange").toString(), ChatColor.GREEN);
                sendMessage(sender, "Player range: " + get(MobSpawnerAbstract.class, m, "requiredPlayerRange").toString(), ChatColor.GREEN);
                for(String s : printNBT("spawnData", getSpawnData(m)))
                {
                    sendMessage(sender, s, ChatColor.GREEN);
                }
            }
            else if(c.equals("mob"))
            {
                String mob;
                if(args.length < 2)
                {
                    mobList(sender);
                    return true;
                }
                if(args[1].equalsIgnoreCase("-f"))
                {
                    if(args.length < 3)
                    {
                        mobList(sender, true);
                        return true;
                    }
                    mob = args[2];
                    if(!checkMob(mob, true))
                    {
                        sendMessage(sender, "This mob does technically not exist.", ChatColor.RED);
                        return true;
                    }
                }
                else
                {
                    mob = args[1];
                    if(!checkMob(mob))
                    {
                        sendMessage(sender, "This mob name can't be used in a spawner.", ChatColor.RED);
                        return true;
                    }
                }
                m.a(mob); // Derpnote
                set(getMobData(m), "c", mob); // Derpnote
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
                    if(set(MobSpawnerAbstract.class, m, "spawnCount", Integer.parseInt(args[1])))
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
                    if(set(MobSpawnerAbstract.class, m, "minSpawnDelay", min) && set(MobSpawnerAbstract.class, m, "maxSpawnDelay", max))
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
                if(args[2].equalsIgnoreCase("string"))
                {
                    value = args[3];
                    for(int i = 4; i < args.length; i++)
                    {
                        value += " " + args[i];
                    }
                }
                else if(!args[2].equals("-") && !args[2].equalsIgnoreCase("list") && !args[2].equalsIgnoreCase("compound"))
                {
                    value = args[3];
                }
                NBTTagCompound base = getSpawnData(m);
                if(base == null)
                {
                    sendMessage(sender, "An error occured, see the console.", ChatColor.RED);
                    return true;
                }
                String string = setNBT(base, args[1], args[2].toLowerCase(), value);
                if(string.length() > 0)
                {
                    sendMessage(sender, string, ChatColor.RED);
                    return true;
                }
                sendMessage(sender, "Data set.", ChatColor.GREEN);
            }
            else if(c.equals("max"))
            {
                if(args.length < 2)
                {
                    sendMessage(sender, "Too few arguments.", ChatColor.RED);
                    return true;
                }
                try
                {
                    if(set(MobSpawnerAbstract.class, m, "maxNearbyEntities", Integer.parseInt(args[1])))
                    {
                        sendMessage(sender, "Set the maximum amount.", ChatColor.GREEN);
                    }
                    else
                    {
                        sendMessage(sender, "Couldn't set the maximum amount, sorry.", ChatColor.YELLOW);
                    }
                }
                catch(Throwable t)
                {
                    sendMessage(sender, "That's not an integer.", ChatColor.RED);
                }
            }
            else if(c.equals("range"))
            {
                if(args.length < 2)
                {
                    sendMessage(sender, "Too few arguments.", ChatColor.RED);
                    return true;
                }
                try
                {
                    if(set(MobSpawnerAbstract.class, m, "spawnRange", Integer.parseInt(args[1])))
                    {
                        sendMessage(sender, "Set the spawn range.", ChatColor.GREEN);
                    }
                    else
                    {
                        sendMessage(sender, "Couldn't set the spawn range, sorry.", ChatColor.YELLOW);
                    }
                }
                catch(Throwable t)
                {
                    sendMessage(sender, "That's not an integer.", ChatColor.RED);
                }
            }
            else if(c.equals("playerrange"))
            {
                if(args.length < 2)
                {
                    sendMessage(sender, "Too few arguments.", ChatColor.RED);
                    return true;
                }
                try
                {
                    if(set(MobSpawnerAbstract.class, m, "requiredPlayerRange", Integer.parseInt(args[1])))
                    {
                        sendMessage(sender, "Set the required player range.", ChatColor.GREEN);
                    }
                    else
                    {
                        sendMessage(sender, "Couldn't set the required player range, sorry.", ChatColor.YELLOW);
                    }
                }
                catch(Throwable t)
                {
                    sendMessage(sender, "That's not an integer.", ChatColor.RED);
                }
            }
            else
            {
                sendMessage(sender, "Unknown command, use /edit help", ChatColor.RED);
            }
        }
        catch(Throwable ttt)
        {
            ttt.printStackTrace();
            sendMessage(sender, "An error occured, it has been logged to the console.", ChatColor.RED);
        }
        return true;
    }
    
    private TileEntityMobSpawnerData getMobData(MobSpawnerAbstract m)
    {
        TileEntityMobSpawnerData data = m.i(); // Derpnote
        if(data == null)
        {
            data = new TileEntityMobSpawnerData(m, new NBTTagCompound(), m.getMobName());
            m.a(data); // Derpnote
        }
        return data;
    }
    
    private NBTTagCompound getSpawnData(MobSpawnerAbstract m)
    {
        return getMobData(m).b; // Derpnote;
    }
    
    private void mobList(CommandSender sender)
    {
        mobList(sender, false);
    }
    
    private void mobList(CommandSender sender, boolean all)
    {
        sendMessage(sender, "Possible mobs are:", ChatColor.GREEN);
        String string = "";
        for(String mob : eList.keySet().toArray(new String[0]))
        {
            if(checkMob(mob, all))
            {
                string += mob + ", ";
            }
        }
        sendMessage(sender, string, ChatColor.AQUA);
    }
    
    private boolean checkMob(String mob)
    {
        return checkMob(mob, false);
    }
    
    private boolean checkMob(String mob, boolean all)
    {
        Class c = eList.get(mob);
        if(c == null)
        {
            return false;
        }
        return ((c.getModifiers() & 1024) == 0) && (all || hasSuperclass(c, EntityLiving.class));
    }
    
    public void run()
    {
        if(checkUpdate())
        {
            getServer().getScheduler().cancelTasks(this);
        }
    }
    
    public boolean checkUpdate()
    {
        _update = hasUpdate("ispawner", _version);
        return _update;
    }
}