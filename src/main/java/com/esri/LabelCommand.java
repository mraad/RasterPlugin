package com.esri;

import com.google.common.io.LineReader;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 */
public class LabelCommand implements CommandExecutor
{
    private final RasterPlugin m_rasterPlugin;

    public LabelCommand(final RasterPlugin rasterPlugin)
    {
        m_rasterPlugin = rasterPlugin;
    }

    @Override
    public boolean onCommand(
            final CommandSender commandSender,
            final Command command,
            final String label,
            final String[] args)
    {
        if (args.length == 2)
        {
            final String worldName = args[0];
            final World world = commandSender.getServer().getWorld(worldName);
            try
            {
                final RasterInfo rasterInfo = m_rasterPlugin.rasterInfoMap.get(worldName);
                if (rasterInfo != null)
                {
                    loadLabels(world, rasterInfo, args[1]);
                }
                else
                {
                    commandSender.sendMessage("Invalid world '" + worldName + "'");
                }
                return true;
            }
            catch (IOException e)
            {
                m_rasterPlugin.getLogger().log(Level.SEVERE, e.toString(), e);
            }
        }
        else
        {
            commandSender.sendMessage("Invalid number of arguments, type 'help' !");
        }
        return false;
    }

    private void loadLabels(
            final World world,
            final RasterInfo rasterInfo,
            final String filename) throws IOException
    {
        final Pattern pattern = Pattern.compile("(\\d+)\\t\\((-?\\d+\\.\\d+),\\s+(-?\\d+\\.\\d+)\\)\\t(.+)");
        final FileReader readable = new FileReader(filename);
        try
        {
            final LineReader lineReader = new LineReader(readable);
            String line;
            while ((line = lineReader.readLine()) != null)
            {
                final Matcher matcher = pattern.matcher(line);
                if (matcher.matches())
                {
                    final String fid = matcher.group(1);
                    final double lon = Double.parseDouble(matcher.group(2));
                    final double lat = Double.parseDouble(matcher.group(3));
                    final String label = matcher.group(4);
                    final int x = rasterInfo.toCol(lon);
                    final int z = rasterInfo.toRow(lat);
                    final Block block = world.getHighestBlockAt(x, z);
                    if (!block.isLiquid())
                    {
                        block.setType(Material.SIGN_POST);
                        final BlockState state = block.getState();
                        if (state instanceof Sign)
                        {
                            final Sign sign = (Sign) state;
                            sign.setLine(0, label);
                            sign.update();
                        }
                    }
                }
            }
        }
        finally
        {
            readable.close();
        }
    }
}
