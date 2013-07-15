package com.esri;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 */
public class PosCommand implements CommandExecutor
{
    private RasterPlugin m_plugin;

    public PosCommand(final RasterPlugin plugin)
    {
        m_plugin = plugin;
    }

    @Override
    public boolean onCommand(
            final CommandSender commandSender,
            final Command command,
            final String label,
            final String[] args)
    {
        boolean rc = false;
        if (commandSender instanceof Player)
        {
            final Player player = (Player) commandSender;
            if (args.length == 0)
            {
                double x, z;
                final Location location = player.getLocation();
                final World world = player.getWorld();
                final RasterInfo rasterInfo = m_plugin.rasterInfoMap.get(world.getName());
                if (rasterInfo != null)
                {
                    x = rasterInfo.toX(location.getX());
                    z = rasterInfo.toZ(location.getZ());
                }
                else
                {
                    x = location.getX();
                    z = location.getZ();
                }
                player.sendMessage(String.format("You are at %.3f %.3f %.3f", x, location.getY(), z));
                rc = true;
            }
            else if (args.length == 2)
            {
                try
                {
                    final double x = Double.parseDouble(args[0]);
                    final double z = Double.parseDouble(args[1]);
                    final World world = player.getWorld();
                    final RasterInfo rasterInfo = m_plugin.rasterInfoMap.get(world.getName());
                    if (rasterInfo != null)
                    {
                        player.teleport(rasterInfo.toLocation(world, x, z));
                        rc = true;
                    }
                    else
                    {
                        player.sendMessage("Invalid world !");
                    }
                }
                catch (NumberFormatException e)
                {
                    player.sendMessage("Invalid location !");
                }
            }
            else if (args.length == 3)
            {
                try
                {
                    final double x = Double.parseDouble(args[0]);
                    final double y = Double.parseDouble(args[1]);
                    final double z = Double.parseDouble(args[2]);

                    player.teleport(new Location(player.getWorld(), x, y, z));
                    rc = true;
                }
                catch (NumberFormatException e)
                {
                    player.sendMessage("Invalid location !");
                }
            }
            else if (args.length == 1 && "max".equalsIgnoreCase(args[0]))
            {
                final World world = player.getWorld();
                final RasterInfo rasterInfo = m_plugin.rasterInfoMap.get(world.getName());
                if (rasterInfo != null)
                {
                    player.teleport(rasterInfo.toMaxLocation(world));
                    rc = true;
                }
                else
                {
                    player.sendMessage("Invalid world !");
                }
            }
        }
        return rc;
    }
}
