// Bukkit Plugin "iSpawner" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.ispawner;

import java.util.*;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import net.drgnome.nbtlib.*;

public class Spawner
{
    private static final String _classNameNBT;
    private static final String _classNameSpawner;
    private Object _tileEntity;
    private Map<String, Tag> _nbt;
    
    static
    {
        String tmp;
        try
        {
            tmp = NBTLib.getMinecraftPackage() + "NBTTagCompound";
        }
        catch(Throwable t)
        {
            tmp = "";
            t.printStackTrace();
        }
        _classNameNBT = tmp;
        try
        {
            tmp = NBTLib.getMinecraftPackage() + "MobSpawnerAbstract";
        }
        catch(Throwable t)
        {
            tmp = "";
            t.printStackTrace();
        }
        _classNameSpawner = tmp;
    }
    
    public Spawner(Object tileEntity)
    {
        _tileEntity = tileEntity;
        try
        {
            Object nbt = NBTLib.instantiateMinecraft("NBTTagCompound", new Object[0], new Object[0]);
            NBTLib.invokeMinecraft("TileEntityMobSpawner", _tileEntity, "b", new String[]{_classNameNBT}, new Object[]{nbt});
            disable(true);
            _nbt = NBT.NBTToMap(nbt);
        }
        catch(Throwable t)
        {
            t.printStackTrace();
        }
    }
    
    public Map<String, Tag> getData()
    {
        return _nbt;
    }
    
    public void setData(Map<String, Tag> map)
    {
        _nbt = map;
    }
    
    public boolean apply()
    {
        try
        {
            int x = (Integer)NBTLib.fetchMinecraftField("TileEntity", _tileEntity, "x");
            int y = (Integer)NBTLib.fetchMinecraftField("TileEntity", _tileEntity, "y");
            int z = (Integer)NBTLib.fetchMinecraftField("TileEntity", _tileEntity, "z");
            Object nbt = NBTLib.instantiateMinecraft("NBTTagCompound", new Object[0], new Object[0]);
            NBTLib.invokeMinecraft("TileEntityMobSpawner", _tileEntity, "b", new String[]{_classNameNBT}, new Object[]{nbt});
            try
            {
                NBTLib.invokeMinecraft("TileEntityMobSpawner", _tileEntity, "a", new String[]{_classNameNBT}, new Object[]{NBT.mapToNBT("", _nbt)});
            }
            catch(Throwable t1)
            {
                NBTLib.invokeMinecraft("TileEntityMobSpawner", _tileEntity, "a", new String[]{_classNameNBT}, new Object[]{nbt});
                throw t1;
            }
            disable(false);
            NBTLib.putMinecraftField("TileEntity", _tileEntity, "x", x);
            NBTLib.putMinecraftField("TileEntity", _tileEntity, "y", y);
            NBTLib.putMinecraftField("TileEntity", _tileEntity, "z", z);
        }
        catch(Throwable t)
        {
            t.printStackTrace();
            return false;
        }
        return true;
    }
    
    public void disable(boolean flag)
    {
        try
        {
            NBTLib.putMinecraftField("MobSpawnerAbstract", NBTLib.fetchDynamicMinecraftField("TileEntityMobSpawner", _tileEntity, _classNameSpawner), "spawnDelay", flag ? Integer.MAX_VALUE : 0);
        }
        catch(Throwable t)
        {
            t.printStackTrace();
        }
    }
}