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
public class CntryGenerator extends ChunkGenerator
{
    private static final byte BEDROCK = (byte) Material.BEDROCK.getId();
    private static final byte WATER = (byte) Material.STATIONARY_WATER.getId();
    private static final byte GRASS = (byte) Material.GRASS.getId();

    private final RasterInfo m_rasterInfo;
    private final int m_maxHeight;

    public CntryGenerator(
            final RasterInfo rasterInfo,
            final int maxHeight)
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
        return !block.isLiquid();
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

                int yy = 0;

                setBlockAt(chunk, x, yy++, z, BEDROCK);
                final int index = sz * m_rasterInfo.ncols + sx;
                if (index > -1 && index < m_rasterInfo.data.length)
                {
                    final float datum = m_rasterInfo.data[index];
                    if (datum == m_rasterInfo.nodata)
                    {
                        setBlockAt(chunk, x, yy++, z, WATER);
                    }
                    else
                    {
                        int count = 1 + (int) (m_maxHeight * (datum - m_rasterInfo.minData) / m_rasterInfo.delData);
                        while (count-- > 0)
                        {
                            setBlockAt(chunk, x, yy++, z, GRASS);
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
