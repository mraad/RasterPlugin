package com.esri;

import org.bukkit.Location;
import org.bukkit.World;

/**
 */
public class RasterInfo
{
    public double minx;
    public double miny;
    public int maxr;
    public int maxc;
    public int ncols;
    public int nrows;
    public float nodata;
    public double cellSize;
    public boolean isLSBFirst;
    public float minData;
    public float maxData;
    public float delData;
    public float[] data;

    public Location toMaxLocation(final World world)
    {
        return new Location(world, maxc,
                world.getHighestBlockYAt(maxc, maxr) + 1
                , maxr);
    }

    public Location toLocation(
            final World world,
            final double x,
            final double z)
    {
        final int col = toCol(x);
        final int row = toRow(z);

        return new Location(world, col,
                world.getHighestBlockYAt(col, row) + 1
                , row);
    }

    public int toCol(final double x)
    {
        return (int) ((x - minx) / cellSize);
    }

    public int toRow(final double z)
    {
        return nrows - (int) ((z - miny) / cellSize);
    }

    public double toX(final double col)
    {
        return minx + col * cellSize;
    }

    public double toZ(final double row)
    {
        return miny + (nrows - row) * cellSize;
    }
}
