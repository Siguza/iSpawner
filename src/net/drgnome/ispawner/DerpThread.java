// Bukkit Plugin "iSpawner" by Siguza
// This software is distributed under the following license:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.ispawner;

public class DerpThread extends Thread
{
    private DerpPlugin plugin;
    
    public DerpThread(DerpPlugin plugin)
    {
        super();
        this.plugin = plugin;
    }
    
    public void run()
    {
        if(plugin != null)
        {
            plugin.tick();
        }
    }
}