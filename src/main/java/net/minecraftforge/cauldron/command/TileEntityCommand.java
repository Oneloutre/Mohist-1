package net.minecraftforge.cauldron.command;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.cauldron.configuration.BoolSetting;
import net.minecraftforge.cauldron.configuration.IntSetting;
import net.minecraftforge.cauldron.configuration.Setting;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;

public class TileEntityCommand extends Command
{
    private static final List<String> COMMANDS = ImmutableList.of("get", "set", "save", "reload");

    public TileEntityCommand()
    {
        super("cauldron_te");
        this.description = "Toggle certain TileEntity options";

        this.usageMessage = "/cauldron_te [" + StringUtils.join(COMMANDS, '|') + "] <option> [value]";
        this.setPermission("cauldron.command.cauldron_te");
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args)
    {
        if (!testPermission(sender))
        {
            return true;
        }
        if ((args.length == 1) && "save".equalsIgnoreCase(args[0]))
        {
            MinecraftServer.getServer().tileEntityConfig.save();
            sender.sendMessage(ChatColor.GREEN + "Config file saved");
            return true;
        }
        if ((args.length == 1) && "reload".equalsIgnoreCase(args[0]))
        {
            MinecraftServer.getServer().tileEntityConfig.load();
            sender.sendMessage(ChatColor.GREEN + "Config file reloaded");
            return true;
        }
        if (args.length < 2)
        {
            sender.sendMessage(ChatColor.RED + "Usage: " + usageMessage);
            return false;
        }

        if ("get".equalsIgnoreCase(args[0]))
        {
            return getToggle(sender, args);
        }
        else if ("set".equalsIgnoreCase(args[0]))
        {
            return setToggle(sender, args);
        }
        else
        {
            sender.sendMessage(ChatColor.RED + "Usage: " + usageMessage);
        }

        return false;
    }

    private boolean getToggle(CommandSender sender, String[] args)
    {
        try
        {
            Setting toggle = MinecraftServer.getServer().tileEntityConfig.getSettings().get(args[1]);
            // check config directly
            if (toggle == null && MinecraftServer.getServer().tileEntityConfig.isSet(args[1]))
            {
                if (MinecraftServer.getServer().tileEntityConfig.isBoolean(args[1]))
                {
                    toggle = new BoolSetting(MinecraftServer.getServer().tileEntityConfig, args[1], MinecraftServer.getServer().tileEntityConfig.getBoolean(args[1], false), "");
                }
                else if (MinecraftServer.getServer().tileEntityConfig.isInt(args[1]))
                {
                    toggle = new IntSetting(MinecraftServer.getServer().tileEntityConfig, args[1], MinecraftServer.getServer().tileEntityConfig.getInt(args[1], 1), "");
                }
                if (toggle != null)
                {
                    MinecraftServer.getServer().tileEntityConfig.getSettings().put(toggle.path, toggle);
                    MinecraftServer.getServer().tileEntityConfig.load();
                }
            }
            if (toggle == null)
            {
                sender.sendMessage(ChatColor.RED + "Could not find option: " + args[1]);
                return false;
            }
            Object value = toggle.getValue();
            String option = (Boolean.TRUE.equals(value) ? ChatColor.GREEN : ChatColor.RED) + " " + value;
            sender.sendMessage(ChatColor.GOLD + args[1] + " " + option);
        }
        catch (Exception ex)
        {
            sender.sendMessage(ChatColor.RED + "Usage: " + usageMessage);
            ex.printStackTrace();
        }
        return true;
    }

    private boolean intervalSet(CommandSender sender, String[] args)
    {
        try
        {
            int setting = NumberUtils.toInt(args[2], 1);
            MinecraftServer.getServer().tileEntityConfig.set(args[1], setting);
        }
        catch (Exception ex)
        {
            sender.sendMessage(ChatColor.RED + "Usage: " + usageMessage);
            return false;
        }
        return true;
    }

    private boolean setToggle(CommandSender sender, String[] args)
    {
        try
        {
            Setting toggle = MinecraftServer.getServer().tileEntityConfig.getSettings().get(args[1]);
            // check config directly
            if (toggle == null && MinecraftServer.getServer().tileEntityConfig.isSet(args[1]))
            {
                toggle = new BoolSetting(MinecraftServer.getServer().tileEntityConfig, args[1], MinecraftServer.getServer().tileEntityConfig.getBoolean(args[1], false), "");
                MinecraftServer.getServer().tileEntityConfig.getSettings().put(toggle.path, toggle);
                MinecraftServer.getServer().tileEntityConfig.load();
            }
            if (toggle == null)
            {
                sender.sendMessage(ChatColor.RED + "Could not find option: " + args[1]);
                return false;
            }
            if (args.length == 2)
            {
                sender.sendMessage(ChatColor.RED + "Usage: " + args[0] + " " + args[1] + " [value]");
                return false;
            }
            toggle.setValue(args[2]);
            Object value = toggle.getValue();
            String option = (Boolean.TRUE.equals(value) ? ChatColor.GREEN : ChatColor.RED) + " " + value;
            sender.sendMessage(ChatColor.GOLD + args[1] + " " + option);
            MinecraftServer.getServer().tileEntityConfig.save();
        }
        catch (Exception ex)
        {
            sender.sendMessage(ChatColor.RED + "Usage: " + usageMessage);
            ex.printStackTrace();
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args)
    {
        Validate.notNull(sender, "Sender cannot be null");
        Validate.notNull(args, "Arguments cannot be null");
        Validate.notNull(alias, "Alias cannot be null");

        if (args.length == 1)
        {
            return StringUtil.copyPartialMatches(args[0], COMMANDS, new ArrayList<String>(COMMANDS.size()));
        }
        if (((args.length == 2) && "get".equalsIgnoreCase(args[0])) || "set".equalsIgnoreCase(args[0]))
        {
            return StringUtil.copyPartialMatches(args[1], MinecraftServer.getServer().tileEntityConfig.getSettings().keySet(), new ArrayList<String>(MinecraftServer.getServer().tileEntityConfig.getSettings().size()));
        }

        return ImmutableList.of();
    }

}
