package com.esri;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 */
public class RasterGenerator extends ChunkGenerator
{
    private static final byte BEDROCK = (byte) Material.BEDROCK.getId();
    private static final byte WATER = (byte) Material.STATIONARY_WATER.getId();
    private static final byte DIRT = (byte) Material.DIRT.getId();
    private static final byte GRASS = (byte) Material.GRASS.getId();
    private static final byte LAVA = (byte) Material.STATIONARY_LAVA.getId();
    // http://www.minecraftwiki.net/wiki/Data_values#Wool
    private static final byte ORANGE = (byte) Material.WOOL.getNewData((byte) 1).getItemTypeId();

    private RasterInfo m_rasterInfo;
    private int m_maxHeight;

    public RasterGenerator(
            final RasterInfo rasterInfo,
            final int maxHeight
    )
    {
        m_rasterInfo = rasterInfo;
        m_maxHeight = maxHeight;
    }

    @Override
    public boolean canSpawn(
            final World world,
            final int x,
            final int z)
    {
        final Block block = world.getHighestBlockAt(x, z);
        return !block.isLiquid() && block.getTypeId() != LAVA;
    }

    private void setBlockAt(
            byte[][] chunk,
            int x,
            int y,
            int z,
            byte typeId)
    {
        chunk[y >> 4][((y & 0xF) << 8) | (z << 4) | x] = typeId;
    }

    @Override
    public byte[][] generateBlockSections(
            final World world,
            final Random random,
            final int chunkX,
            final int chunkZ,
            final BiomeGrid biomeGrid)
    {
        final int startX = chunkX << 4;
        final int startZ = chunkZ << 4;

        final byte[][] chunk = new byte[8][4096];

        for (int x = 0; x < 16; ++x)
        {
            final int sx = startX + x;

            for (int z = 0; z < 16; ++z)
            {
                final int sz = startZ + z;

                int y = 0;

                setBlockAt(chunk, x, y++, z, BEDROCK);

                final int index = sz * m_rasterInfo.ncols + sx;
                if (index > -1 && index < m_rasterInfo.data.length)
                {
                    final float datum = m_rasterInfo.data[index];
                    if (datum == m_rasterInfo.nodata)
                    {
                        setBlockAt(chunk, x, y++, z, WATER);
                    }
                    else
                    {
                        int count = (int) (m_maxHeight * (datum - m_rasterInfo.minData) / m_rasterInfo.delData);
                        if (count < 2)
                        {
                            count = 2;
                            while (count-- > 0)
                            {
                                setBlockAt(chunk, x, y++, z, WATER);
                            }
                        }
                        else if (count > 48)
                        {
                            int diff = count - 48;
                            count = 48;
                            while (count-- > 0)
                            {
                                setBlockAt(chunk, x, y++, z, DIRT);
                            }
                            while (diff-- > 0)
                            {
                                setBlockAt(chunk, x, y++, z, LAVA);
                            }
                        }
                        else
                        {
                            while (count-- > 0)
                            {
                                setBlockAt(chunk, x, y++, z, DIRT);
                            }
                            setBlockAt(chunk, x, y++, z, GRASS);
                        }
                    }
                }
            }
        }

        return chunk;
    }

    @Override
    public List<BlockPopulator> getDefaultPopulators(final World world)
    {
        return new ArrayList<BlockPopulator>();
    }
}
