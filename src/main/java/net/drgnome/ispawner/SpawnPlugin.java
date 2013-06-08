// Bukkit Plugin "iSpawner" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.ispawner;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.block.Block;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.*;
import org.bukkit.event.block.Action;
import net.drgnome.nbtlib.*;

public class SpawnPlugin extends JavaPlugin implements Runnable, Listener
{
    public static final String _version = "#VERSION#";
    private static SpawnPlugin _instance;
    private final ConcurrentHashMap<String, Spawner> _activeSessions = new ConcurrentHashMap<String, Spawner>();
    private final ArrayList<String> _waitingSessions = new ArrayList<String>();
    private boolean _update = false;
    
    public SpawnPlugin()
    {
        super();
        _instance = this;
    }
    
    public static SpawnPlugin instance()
    {
        return _instance;
    }
    
    public void onEnable()
    {
        Util._log.info("Enabling iSpawner " + _version);
        Config.reload();
        getServer().getPluginManager().registerEvents(this, this);
        if(Config.bool("check-update"))
        {
            getServer().getScheduler().scheduleSyncRepeatingTask(this, this, 0L, 72000L);
        }
    }
    
    public void onDisable()
    {
        getServer().getScheduler().cancelTasks(this);
        Util._log.info("Disabling iSpawner " + _version);
    }
    
    public void reloadConfig()
    {
        super.reloadConfig();
        Config.reload();
    }
    
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        Commands.handle(sender, args);
        return true;
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void handleClick(PlayerInteractEvent event)
    {
        if(!event.hasBlock() || ((event.getAction() != Action.LEFT_CLICK_BLOCK) && (event.getAction() != Action.RIGHT_CLICK_BLOCK)) || !tryStartSession(event.getPlayer().getName()))
        {
            return;
        }
        Block block = event.getClickedBlock();
        startSession(event.getPlayer(), block.getWorld(), block.getX(), block.getY(), block.getZ());
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void handleLogin(PlayerLoginEvent event)
    {
        if(_update && event.getPlayer().hasPermission("ispawner.update"))
        {
            Util.sendMessage(event.getPlayer(), "There is an update for iSpawner available!", ChatColor.GOLD);
        }
    }
    
    public void prepareSession(String name)
    {
        name = name.toLowerCase();
        if(!_waitingSessions.contains(name))
        {
            _waitingSessions.add(name);
        }
    }
    
    public boolean tryStartSession(String name)
    {
        return _waitingSessions.remove(name.toLowerCase());
    }
    
    public void startSession(CommandSender sender, World world, int x, int y, int z)
    {
        if(world.getBlockTypeIdAt(x, y, z) != Material.MOB_SPAWNER.getId())
        {
            Util.sendMessage(sender, "That's not a mob spawner.", ChatColor.RED);
            return;
        }
        Object mcWorld;
        try
        {
            mcWorld = NBTLib.invokeCraftbukkit("CraftWorld", world, "getHandle", new Object[0], new Object[0]);
        }
        catch(Throwable t)
        {
            Util.sendMessage(sender, "Error fetching the world handle. See console.", ChatColor.RED);
            t.printStackTrace();
            return;
        }
        Object tilEntity;
        try
        {
            tilEntity = NBTLib.invokeMinecraft("World", mcWorld, "getTileEntity", new Class[]{int.class, int.class, int.class}, new Object[]{x, y, z});
        }
        catch(Throwable t)
        {
            Util.sendMessage(sender, "Error fetching tile entity. See console.", ChatColor.RED);
            t.printStackTrace();
            return;
        }
        _activeSessions.put(sender.getName().toLowerCase(), new Spawner(tilEntity));
        Util.sendMessage(sender, "Editing session started.", ChatColor.GREEN);
    }
    
    public boolean hasSession(String name)
    {
        return _activeSessions.containsKey(name.toLowerCase());
    }
    
    public Spawner getSession(String name)
    {
        name = name.toLowerCase();
        return _activeSessions.containsKey(name) ? _activeSessions.get(name) : null;
    }
    
    public void endSession(String name)
    {
        _activeSessions.remove(name.toLowerCase());
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
        _update = Util.hasUpdate("ispawner", _version);
        return _update;
    }
}