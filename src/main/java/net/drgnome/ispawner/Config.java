// Bukkit Plugin "iSpawner" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.ispawner;

import org.bukkit.configuration.file.FileConfiguration;

public class Config
{
    private static FileConfiguration _config;
    
    public static void reload()
    {
        _config = SpawnPlugin.instance().getConfig();
        setDefs();
        SpawnPlugin.instance().saveConfig();
    }
    
    private static void setDefs()
    {
        setDef("check-update", "true");
    }
    
    private static void setDef(String key, Object value)
    {
        if(!_config.isSet(key))
        {
            _config.set(key, value);
        }
    }
    
    public static boolean bool(String key)
    {
        return _config.getString(key).equalsIgnoreCase("true");
    }
}