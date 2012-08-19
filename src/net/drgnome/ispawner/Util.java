// Bukkit Plugin "iSpawner" by Siguza
// This software is distributed under the following license:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.ispawner;

import net.minecraft.server.*;

import java.lang.reflect.*;
import java.util.*;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class Util
{
    public static final String LS = System.getProperty("line.separator");
    public static Logger log = Logger.getLogger("Minecraft");
    
    // These 3 methods split up strings into multiple lines so that the message doesn't get messed up by the minecraft chat.
    // You can also give a prefix that is set before every line.
    public static void sendMessage(CommandSender sender, String message)
    {
        sendMessage(sender, message, "");
    }
    
    public static void sendMessage(CommandSender sender, String message, ChatColor prefix)
    {
        sendMessage(sender, message, "" + prefix);
    }
    
    public static void sendMessage(CommandSender sender, String message, String prefix)
    {
        if((sender == null) || (message == null))
        {
            return;
        }
        if(prefix == null)
        {
            prefix = "";
        }
        int offset = 0;
        int xpos = 0;
        int pos = 0;
        String part;
        while(true)
        {
            if(offset + 60 >= message.length())
            {
                sender.sendMessage(prefix + message.substring(offset, message.length()));
                break;
            }
            part = message.substring(offset, offset + 60);
            xpos = part.lastIndexOf(" ");
            pos = xpos < 0 ? 60 : xpos;
            part = message.substring(offset, offset + pos);
            sender.sendMessage(prefix + part);
            offset += pos + (xpos < 0 ? 0 : 1);
        }
    }
    
    // Before e.printStackTrace:
    public static void warn()
    {
        log.warning("[iSpawner] AN ERROR OCCURED! PLEASE SEND THE MESSAGE BELOW TO THE DEVELOPER!");
    }
    
    public static Object get(Object o, String s)
    {
        try
        {
            Field field = o.getClass().getDeclaredField(s);
            field.setAccessible(true);
            return field.get(o);
        }
        catch(Throwable t)
        {
            warn();
            t.printStackTrace();
            return null;
        }
    }
    
    public static boolean set(Object o, String s, Object value)
    {
        try
        {
            Field field = o.getClass().getDeclaredField(s);
            field.setAccessible(true);
            field.set(o, value);
            return true;
        }
        catch(Throwable t)
        {
            warn();
            t.printStackTrace();
            return false;
        }
    }
    
    public static String[] printNBT(String name, NBTBase base)
    {
        if(base == null)
        {
            return new String[0];
        }
        String string = name + ": ";
        switch(base.getTypeId())
        {
            case 1:
                string += "(byte) " + ((NBTTagByte)base).toString();
                break;
            case 2:
                string += "(short) " + ((NBTTagShort)base).toString();
                break;
            case 3:
                string += "(int) " + ((NBTTagInt)base).toString();
                break;
            case 4:
                string += "(long) " + ((NBTTagLong)base).toString();
                break;
            case 5:
                string += "(float) " + ((NBTTagFloat)base).toString();
                break;
            case 6:
                string += "(double) " + ((NBTTagDouble)base).toString();
                break;
            case 8:
                string += "(string) " + ((NBTTagByte)base).toString();
                break;
            case 9:
                ArrayList<String> list = new ArrayList<String>();
                list.add(string + "(list)");
                Object o = get(base, "list");
                if((o != null) || (o instanceof ArrayList))
                {
                    NBTBase newbase[] = ((ArrayList<NBTBase>)o).toArray(new NBTBase[0]);
                    for(int i = 0; i < newbase.length; i++)
                    {
                        for(String append : printNBT("" + i, newbase[i]))
                        {
                            list.add(name + "." + append);
                        }
                    }
                    
                }
                return list.toArray(new String[0]);
            case 10:
                ArrayList<String> list1 = new ArrayList<String>();
                list1.add(string + "(compound)");
                Object o1 = get(base, "map");
                if((o1 != null) || (o1 instanceof HashMap))
                {
                    Iterator it = ((HashMap<String, NBTBase>)o1).entrySet().iterator();
                    Map.Entry<String, NBTBase> entry;
                    while(it.hasNext())
                    {
                        entry = (Map.Entry<String, NBTBase>)it.next();
                        for(String append : printNBT(entry.getKey(), entry.getValue()))
                        {
                            list1.add(name + "." + append);
                        }
                    }
                    
                }
                return list1.toArray(new String[0]);
            default:
                return new String[0];
        }
        return new String[]{string};
    }
    
    public static String setNBT(NBTBase base, String path, String type, String value)
    {
        try
        {
            if((base == null) || (path == null) || (type == null) || (value == null))
            {
                return "Stop it.";
            }
            String var[] = path.split("\\.");
            if(!(base instanceof NBTTagCompound) && !(base instanceof NBTTagList))
            {
                return base.getName() + " is neither a compound nor list.";
            }
            if(var.length > 1)
            {
                try
                {
                    NBTBase newbase = (base instanceof NBTTagCompound) ? ((NBTTagCompound)base).get(var[0]) : ((NBTTagList)base).get(Integer.parseInt(var[0]));
                    return setNBT(newbase, path.substring(path.indexOf(".") + 1), type, value);
                }
                catch(NumberFormatException e)
                {
                    return "Invalid list key.";
                }
                catch(IndexOutOfBoundsException e)
                {
                    return "List index out of bounds.";
                }
            }
            else
            {
                if(base instanceof NBTTagCompound)
                {
                    Object o = get(base, "map");
                    if((o == null) || !(o instanceof HashMap))
                    {
                        return "Can't access compound " + base.getName() + ".";
                    }
                    HashMap<String, NBTBase> map = (HashMap<String, NBTBase>)o;
                    if(type.equals("-"))
                    {
                        if(map.remove(var[0]) == null)
                        {
                            return "This doesn't even exist.";
                        }
                    }
                    else
                    {
                        NBTBase newnbt = createNBT(var[0], type, value);
                        if(newnbt == null)
                        {
                            return type + " and " + value + " don't match somewhere.";
                        }
                        map.put(var[0], newnbt);
                    }
                    set(base, "map", map);
                }
                else
                {
                    Object o = get(base, "list");
                    if((o == null) || !(o instanceof ArrayList))
                    {
                        return "Can't access list " + base.getName() + ".";
                    }
                    ArrayList<NBTBase> list = (ArrayList<NBTBase>)o;
                    int i = 0;
                    try
                    {
                        i = Integer.parseInt(var[0]);
                    }
                    catch(NumberFormatException e)
                    {
                        return "Invalid list key";
                    }
                    if(type.equals("-"))
                    {
                        if((i < 0) || (i >= list.size()))
                        {
                            return "List index out of bounds.";
                        }
                        list.remove(i);
                    }
                    else
                    {
                        if(i < 0)
                        {
                            return "List index out of bounds.";
                        }
                        NBTBase newnbt = createNBT(var[0], type, value);
                        if(newnbt == null)
                        {
                            return type + " and " + value + " don't match somewhere.";
                        }
                        if(i >= list.size())
                        {
                            list.add(newnbt);
                        }
                        else
                        {
                            list.set(i, newnbt);
                        }
                    }
                    set(base, "list", list);
                }
            }
        }
        catch(Throwable t)
        {
            t.printStackTrace();
            return "Error. See console.";
        }
        return "";
    }
    
    public static NBTBase createNBT(String name, String type, String value)
    {
        try
        {
            if(type.equals("byte"))
            {
                return new NBTTagByte(name, Byte.parseByte(value));
            }
            if(type.equals("short"))
            {
                return new NBTTagShort(name, Short.parseShort(value));
            }
            if(type.equals("int"))
            {
                return new NBTTagInt(name, Integer.parseInt(value));
            }
            if(type.equals("long"))
            {
                return new NBTTagLong(name, Long.parseLong(value));
            }
            if(type.equals("float"))
            {
                return new NBTTagFloat(name, Float.parseFloat(value));
            }
            if(type.equals("double"))
            {
                return new NBTTagDouble(name, Double.parseDouble(value));
            }
            if(type.equals("string"))
            {
                return new NBTTagString(name, value);
            }
            if(type.equals("list"))
            {
                return new NBTTagList(name);
            }
            if(type.equals("compound"))
            {
                return new NBTTagCompound(name);
            }
        }
        catch(Throwable t)
        {
            warn();
            t.printStackTrace();
            return null;
        }
        return null;
    }
    
    public static boolean hasSuperclass(Class leBase, Class leSuper)
    {
        if(leBase.getSuperclass() == leSuper)
        {
            return true;
        }
        if(leBase.getSuperclass() == Object.class.getSuperclass())
        {
            return false;
        }
        return hasSuperclass(leBase.getSuperclass(), leSuper);
    }
}