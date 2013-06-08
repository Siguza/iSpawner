// Bukkit Plugin "iSpawner" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.ispawner;

import java.util.*;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import net.drgnome.nbtlib.*;

public class NBTHelper
{
    public static void parse(CommandSender sender, Map<String, Tag> map, String line)
    {
        parse(sender, map, line, 0);
    }
    
    public static void parse(CommandSender sender, Map<String, Tag> map, String line, int num)
    {
        String[] parts = line.split(" ", 3);
        if(parts.length < 2)
        {
            Util.sendMessage(sender, s("Too few arguments.", num), ChatColor.RED);
            return;
        }
        NBT type = getType(parts[1]);
        if(type == null)
        {
            Util.sendMessage(sender, s("Invalid NBT type.", num), ChatColor.RED);
            return;
        }
        else if((parts.length < 3) && (type != NBT.LIST) && (type != NBT.COMPOUND))
        {
            Util.sendMessage(sender, s("Too few arguments.", num), ChatColor.RED);
            return;
        }
        Tag tag;
        if(type == NBT.END)
        {
            tag = null;
        }
        else if(type == NBT.BOOL)
        {
            boolean value;
            if(parts[2].equalsIgnoreCase("true") || parts[2].equalsIgnoreCase("yes"))
            {
                value = true;
            }
            else if(parts[2].equalsIgnoreCase("false") || parts[2].equalsIgnoreCase("no"))
            {
                value = false;
            }
            else
            {
                Util.sendMessage(sender, s("Invalid bool value.", num), ChatColor.RED);
                return;
            }
            tag = Tag.newBool(value);
        }
        else if(type == NBT.BYTE)
        {
            byte value;
            if(parts[2].equalsIgnoreCase("max"))
            {
                value = Byte.MAX_VALUE;
            }
            else
            {
                try
                {
                    value = Byte.parseByte(parts[2]);
                }
                catch(Throwable t)
                {
                    Util.sendMessage(sender, s("Invalid byte value.", num), ChatColor.RED);
                    return;
                }
            }
            tag = Tag.newByte(value);
        }
        else if(type == NBT.SHORT)
        {
            short value;
            if(parts[2].equalsIgnoreCase("max"))
            {
                value = Short.MAX_VALUE;
            }
            else
            {
                try
                {
                    value = Short.parseShort(parts[2]);
                }
                catch(Throwable t)
                {
                    Util.sendMessage(sender, s("Invalid short value.", num), ChatColor.RED);
                    return;
                }
            }
            tag = Tag.newShort(value);
        }
        else if(type == NBT.INT)
        {
            int value;
            if(parts[2].equalsIgnoreCase("max"))
            {
                value = Integer.MAX_VALUE;
            }
            else
            {
                try
                {
                    value = Integer.parseInt(parts[2]);
                }
                catch(Throwable t)
                {
                    Util.sendMessage(sender, s("Invalid int value.", num), ChatColor.RED);
                    return;
                }
            }
            tag = Tag.newInt(value);
        }
        else if(type == NBT.LONG)
        {
            long value;
            if(parts[2].equalsIgnoreCase("max"))
            {
                value = Long.MAX_VALUE;
            }
            else
            {
                try
                {
                    value = Long.parseLong(parts[2]);
                }
                catch(Throwable t)
                {
                    Util.sendMessage(sender, s("Invalid long value.", num), ChatColor.RED);
                    return;
                }
            }
            tag = Tag.newLong(value);
        }
        else if(type == NBT.FLOAT)
        {
            float value;
            if(parts[2].equalsIgnoreCase("max"))
            {
                value = Float.MAX_VALUE;
            }
            else
            {
                try
                {
                    value = Float.parseFloat(parts[2]);
                }
                catch(Throwable t)
                {
                    Util.sendMessage(sender, s("Invalid float value.", num), ChatColor.RED);
                    return;
                }
            }
            tag = Tag.newFloat(value);
        }
        else if(type == NBT.DOUBLE)
        {
            double value;
            if(parts[2].equalsIgnoreCase("max"))
            {
                value = Double.MAX_VALUE;
            }
            else
            {
                try
                {
                    value = Double.parseDouble(parts[2]);
                }
                catch(Throwable t)
                {
                    Util.sendMessage(sender, s("Invalid double value.", num), ChatColor.RED);
                    return;
                }
            }
            tag = Tag.newDouble(value);
        }
        else if(type == NBT.BYTE_ARRAY)
        {
            String[] array = parts[2].split(",");
            byte[] value = new byte[array.length];
            for(int i = 0; i < array.length; i++)
            {
                try
                {
                    array[i].trim();
                    value[i] = Byte.parseByte(array[i]);
                }
                catch(Throwable t)
                {
                    Util.sendMessage(sender, s("Invalid byte_array value: " + array[i], num), ChatColor.RED);
                    return;
                }
            }
            tag = Tag.newByteArray(value);
        }
        else if(type == NBT.STRING)
        {
            tag = Tag.newString(parts[2]);
        }
        else if(type == NBT.LIST)
        {
            try
            {
                tag = Tag.newList(new ArrayList<Tag>());
            }
            catch(Throwable t)
            {
                Util.sendMessage(sender, s("Creating list failed.", num), ChatColor.RED);
                t.printStackTrace();
                return;
            }
        }
        else if(type == NBT.COMPOUND)
        {
            try
            {
                tag = Tag.newCompound(new HashMap<String, Tag>());
            }
            catch(Throwable t)
            {
                Util.sendMessage(sender, s("Creating compound failed.", num), ChatColor.RED);
                t.printStackTrace();
                return;
            }
        }
        else if(type == NBT.INT_ARRAY)
        {
            String[] array = parts[2].split(",");
            int[] value = new int[array.length];
            for(int i = 0; i < array.length; i++)
            {
                try
                {
                    array[i].trim();
                    value[i] = Integer.parseInt(array[i]);
                }
                catch(Throwable t)
                {
                    Util.sendMessage(sender, s("Invalid int_array value: " + array[i], num), ChatColor.RED);
                    return;
                }
            }
            tag = Tag.newIntArray(value);
        }
        else
        {
            Util.sendMessage(sender, s("This NBT tag seems to exist but is so new that iSpawner doesn't know it. Please tell the developer about this.", num), ChatColor.YELLOW);
            return;
        }
        put(sender, map, parts[0], tag, num);
    }
    
    private static void put(CommandSender sender, Map<String, Tag> map, String path, Tag tag, int num)
    {
        String name, pname;
        int pos = path.lastIndexOf(".");
        if(pos == -1)
        {
            name = path;
            path = null;
            pname = "";
        }
        else
        {
            name = path.substring(pos + 1);
            path = path.substring(0, pos);
            pname = path + ".";
        }
        try
        {
            Tag parent = resolve(sender, map, path, num);
            if(parent == null)
            {
                Util.sendMessage(sender, "Can't resolve parent tag.", ChatColor.RED);
                return;
            }
            if(parent.getType() == NBT.LIST)
            {
                int index;
                try
                {
                    index = Integer.parseInt(name);
                }
                catch(Throwable t)
                {
                    Util.sendMessage(sender, s(pname + name + " is an invalid list key.", num), ChatColor.YELLOW);
                    return;
                }
                List<Tag> list = ((Tag<List<Tag>>)parent).get();
                if(tag == null)
                {
                    if(list.size() > index)
                    {
                        list.remove(index);
                        if(num == 0)
                        {
                            Util.sendMessage(sender, pname + name + " removed.", ChatColor.GREEN);
                        }
                    }
                    else
                    {
                        Util.sendMessage(sender, s(pname + name + " is already null.", num), ChatColor.YELLOW);
                    }
                }
                else
                {
                    if(list.size() > index)
                    {
                        list.add(index, tag);
                        if(num == 0)
                        {
                            Util.sendMessage(sender, pname + name + " shifted in.", ChatColor.GREEN);
                        }
                    }
                    else
                    {
                        while(list.size() < index)
                        {
                            list.add(NBT.NBTToTag(NBT.tagToNBT("", tag)));
                        }
                        list.add(tag);
                        if(num == 0)
                        {
                            Util.sendMessage(sender, pname + name + " added.", ChatColor.GREEN);
                        }
                    }
                }
            }
            else if(parent.getType() == NBT.COMPOUND)
            {
                Map<String, Tag> map1 = ((Tag<Map<String, Tag>>)parent).get();
                if(tag == null)
                {
                    if(map1.containsKey(name))
                    {
                        map1.remove(name);
                        if(num == 0)
                        {
                            Util.sendMessage(sender, pname + name + " removed.", ChatColor.GREEN);
                        }
                    }
                    else
                    {
                        Util.sendMessage(sender, s(pname + name + " is already null.", num), ChatColor.YELLOW);
                    }
                }
                else
                {
                    map1.put(name, tag);
                    if(num == 0)
                    {
                        Util.sendMessage(sender, pname + name + " set.", ChatColor.GREEN);
                    }
                }
            }
            else
            {
                Util.sendMessage(sender, s(pname + name + " is neither a list nor a compound.", num), ChatColor.YELLOW);
            }
        }
        catch(Throwable t)
        {
            Util.sendMessage(sender, s("Something went wrong. Better see the console.", num), ChatColor.RED);
        }
    }
    
    public static Tag resolve(CommandSender sender, Map<String, Tag> map, String path)
    {
        return resolve(sender, map, path, 0);
    }
    
    public static Tag resolve(CommandSender sender, Map<String, Tag> map, String path, int num)
    {
        try
        {
            return resolve0(sender, Tag.newCompound(map), path, num, "");
        }
        catch(Throwable t)
        {
            Util.sendMessage(sender, s("Creating compound failed.", num), ChatColor.RED);
            t.printStackTrace();
            return null;
        }
    }
    
    private static Tag resolve0(CommandSender sender, Tag tag, String path, int num, String prefix)
    {
        if(path == null)
        {
            return tag;
        }
        String[] split = path.split("\\.", 2);
        if(tag.getType() == NBT.LIST)
        {
            int index;
            try
            {
                index = Integer.parseInt(split[0]);
            }
            catch(Throwable t)
            {
                Util.sendMessage(sender, s(prefix + split[0] + " is an invalid list key.", num), ChatColor.YELLOW);
                return null;
            }
            List<Tag> list = ((Tag<List<Tag>>)tag).get();
            if(list.size() <= index)
            {
                Util.sendMessage(sender, s(prefix + split[0] + " is null.", num), ChatColor.YELLOW);
                return null;
            }
            return resolve0(sender, list.get(index), (split.length > 1) ? split[1] : null, num, prefix + split[0] + ".");
        }
        else if(tag.getType() == NBT.COMPOUND)
        {
            Map<String, Tag> map = ((Tag<Map<String, Tag>>)tag).get();
            if(!map.containsKey(split[0]))
            {
                Util.sendMessage(sender, s(prefix + split[0] + " is null.", num), ChatColor.YELLOW);
                return null;
            }
            return resolve0(sender, map.get(split[0]), (split.length > 1) ? split[1] : null, num, prefix + split[0] + ".");
        }
        else
        {
            Util.sendMessage(sender, s(prefix + split[0] + " is neither a list nor a compound.", num), ChatColor.YELLOW);
            return null;
        }
    }
    
    public static void print(CommandSender sender, Map<String, Tag> map)
    {
        try
        {
            Util.sendMessage(sender, "========== Spawner Data ==========", ChatColor.BLUE);
            print0(sender, Tag.newCompound(map), "");
        }
        catch(Throwable t)
        {
            Util.sendMessage(sender, "Printing failed.", ChatColor.RED);
            t.printStackTrace();
        }
    }
    
    private static void print0(CommandSender sender, Tag tag, String path)
    {
        NBT type = tag.getType();
        if(type == NBT.LIST)
        {
            Util.sendMessage(sender, ChatColor.GREEN + path + ChatColor.YELLOW + " (" + type.name().toLowerCase() + ")");
            List<Tag> list = ((Tag<List<Tag>>)tag).get();
            for(int i = 0; i < list.size(); i++)
            {
                print0(sender, list.get(i), path + (path.isEmpty() ? "" : ".") + i);
            }
        }
        else if(type == NBT.COMPOUND)
        {
            if(!path.isEmpty())
            {
                Util.sendMessage(sender, ChatColor.GREEN + path + ChatColor.YELLOW + " (" + type.name().toLowerCase() + ")");
            }
            Map<String, Tag> map = ((Tag<Map<String, Tag>>)tag).get();
            for(Map.Entry<String, Tag> entry : map.entrySet())
            {
                print0(sender, entry.getValue(), path + (path.isEmpty() ? "" : ".") + entry.getKey());
            }
        }
        else
        {
            Util.sendMessage(sender, ChatColor.GREEN + path + ChatColor.YELLOW + " (" + type.name().toLowerCase() + ") " + ChatColor.LIGHT_PURPLE + tag.get());
        }
    }
    
    public static void export(CommandSender sender, String[] args)
    {
        String[] lines = export0(sender, SpawnPlugin.instance().getSession(sender.getName()).getData());
        if(lines == null)
        {
            return;
        }
        if(SpawnPlugin.exportData(args[1], lines))
        {
            Util.sendMessage(sender, "Spawner data has been exported to iSpawner/data/" + args[1] + ".txt.", ChatColor.GREEN);
        }
        else
        {
            Util.sendMessage(sender, "Exporting failed.", ChatColor.RED);
        }
    }
    
    private static String[] export0(CommandSender sender, Map<String, Tag> map)
    {
        try
        {
            ArrayList<String> list = new ArrayList<String>();
            export1(list, Tag.newCompound(map), "");
            return list.toArray(new String[0]);
        }
        catch(Throwable t)
        {
            Util.sendMessage(sender, "Exporting failed.", ChatColor.RED);
            t.printStackTrace();
            return null;
        }
    }
    
    private static void export1(List<String> out, Tag tag, String path)
    {
        NBT type = tag.getType();
        if(type == NBT.LIST)
        {
            out.add(path + " " + type.name().toLowerCase());
            List<Tag> list = ((Tag<List<Tag>>)tag).get();
            for(int i = 0; i < list.size(); i++)
            {
                export1(out, list.get(i), path + (path.isEmpty() ? "" : ".") + i);
            }
        }
        else if(type == NBT.COMPOUND)
        {
            if(!path.isEmpty())
            {
                out.add(path + " " + type.name().toLowerCase());
            }
            Map<String, Tag> map = ((Tag<Map<String, Tag>>)tag).get();
            for(Map.Entry<String, Tag> entry : map.entrySet())
            {
                export1(out, entry.getValue(), path + (path.isEmpty() ? "" : ".") + entry.getKey());
            }
        }
        else
        {
            out.add(path + " " + type.name().toLowerCase() + " " + tag.get());
        }
    }
    
    private static NBT getType(String type)
    {
        if(type.equalsIgnoreCase("-"))
        {
            return NBT.END;
        }
        for(NBT val : NBT.values())
        {
            if(val.name().equalsIgnoreCase(type))
            {
                return val;
            }
        }
        return null;
    }
    
    private static String s(String s, int line)
    {
        return ((line > 0) ? "Line " + line + ": " : "") + s;
    }
}